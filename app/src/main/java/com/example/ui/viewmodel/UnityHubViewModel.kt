package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TutorialEntity
import com.example.data.local.UnityHubDatabase
import com.example.data.local.UnityProjectEntity
import com.example.data.repository.UnityHubRepository
import com.example.data.repository.UnityWebUrls
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HubTab {
    PROJECTS,
    TUTORIALS,
    WEBSITE
}

sealed interface HubUiState {
    data object Loading : HubUiState
    data class Ready(
        val activeTab: HubTab = HubTab.PROJECTS,
        val projects: List<UnityProjectEntity> = emptyList(),
        val allProjectCount: Int = 0,
        val projectQuery: String = "",
        val selectedProjectFilter: String = "All",
        val selectedProject: UnityProjectEntity? = null,
        val isCreateProjectDialogOpen: Boolean = false,
        val launchBannerMessage: String? = null,
        val tutorials: List<TutorialEntity> = emptyList(),
        val tutorialQuery: String = "",
        val selectedTutorialCategory: String = "All",
        val selectedTutorial: TutorialEntity? = null,
        val currentWebsiteUrl: String = UnityWebUrls.BRANDING_PT,
        val webViewReloadToken: Int = 0
    ) : HubUiState
}

private data class FilterInputs(
    val activeTab: HubTab = HubTab.PROJECTS,
    val projectQuery: String = "",
    val projectFilter: String = "All",
    val selectedProjectId: Int? = null,
    val isCreateDialogOpen: Boolean = false,
    val launchBannerMessage: String? = null,
    val tutorialQuery: String = "",
    val tutorialCategory: String = "All",
    val selectedTutorialId: Int? = null,
    val currentWebsiteUrl: String = UnityWebUrls.BRANDING_PT,
    val webViewReloadToken: Int = 0
)

class UnityHubViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UnityHubRepository

    private val inputs = MutableStateFlow(FilterInputs())

    val uiState: StateFlow<HubUiState>

    init {
        val dao = UnityHubDatabase.getInstance(application).unityHubDao()
        repository = UnityHubRepository(dao)

        viewModelScope.launch {
            repository.ensureSeeded()
        }

        uiState = combine(
            repository.projects,
            repository.tutorials,
            inputs
        ) { allProjects, allTutorials, filter ->
            val filteredProjects = allProjects.filter { project ->
                val matchesQuery = filter.projectQuery.isBlank() ||
                    project.name.contains(filter.projectQuery, ignoreCase = true) ||
                    project.template.contains(filter.projectQuery, ignoreCase = true) ||
                    project.editorVersion.contains(filter.projectQuery, ignoreCase = true)

                val matchesFilter = when (filter.projectFilter) {
                    "All" -> true
                    "Favorites" -> project.isFavorite
                    "3D URP" -> project.template.contains("3D", ignoreCase = true)
                    "2D" -> project.template.contains("2D", ignoreCase = true)
                    "VR / XR" -> project.template.contains("VR", ignoreCase = true) ||
                        project.targetPlatform.contains("XR", ignoreCase = true)
                    else -> true
                }
                matchesQuery && matchesFilter
            }

            val filteredTutorials = allTutorials.filter { tutorial ->
                val matchesQuery = filter.tutorialQuery.isBlank() ||
                    tutorial.title.contains(filter.tutorialQuery, ignoreCase = true) ||
                    tutorial.summary.contains(filter.tutorialQuery, ignoreCase = true) ||
                    tutorial.category.contains(filter.tutorialQuery, ignoreCase = true)

                val matchesCategory = filter.tutorialCategory == "All" ||
                    tutorial.category == filter.tutorialCategory

                matchesQuery && matchesCategory
            }

            HubUiState.Ready(
                activeTab = filter.activeTab,
                projects = filteredProjects,
                allProjectCount = allProjects.size,
                projectQuery = filter.projectQuery,
                selectedProjectFilter = filter.projectFilter,
                selectedProject = allProjects.find { it.id == filter.selectedProjectId },
                isCreateProjectDialogOpen = filter.isCreateDialogOpen,
                launchBannerMessage = filter.launchBannerMessage,
                tutorials = filteredTutorials,
                tutorialQuery = filter.tutorialQuery,
                selectedTutorialCategory = filter.tutorialCategory,
                selectedTutorial = allTutorials.find { it.id == filter.selectedTutorialId },
                currentWebsiteUrl = filter.currentWebsiteUrl,
                webViewReloadToken = filter.webViewReloadToken
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HubUiState.Loading
        )
    }

    fun selectTab(tab: HubTab) {
        inputs.update { it.copy(activeTab = tab) }
    }

    fun updateProjectQuery(query: String) {
        inputs.update { it.copy(projectQuery = query) }
    }

    fun selectProjectFilter(filter: String) {
        inputs.update { it.copy(projectFilter = filter) }
    }

    fun openProjectDetail(project: UnityProjectEntity) {
        inputs.update { it.copy(selectedProjectId = project.id) }
    }

    fun closeProjectDetail() {
        inputs.update { it.copy(selectedProjectId = null) }
    }

    fun setCreateProjectDialogOpen(open: Boolean) {
        inputs.update { it.copy(isCreateDialogOpen = open) }
    }

    fun createProject(
        name: String,
        template: String,
        editorVersion: String,
        targetPlatform: String,
        description: String
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createProject(name, template, editorVersion, targetPlatform, description)
            inputs.update {
                it.copy(
                    isCreateDialogOpen = false,
                    launchBannerMessage = "Created Unity project \"$name\" ($editorVersion)"
                )
            }
        }
    }

    fun toggleProjectFavorite(project: UnityProjectEntity) {
        viewModelScope.launch {
            repository.toggleProjectFavorite(project)
        }
    }

    fun updateProjectConfig(
        project: UnityProjectEntity,
        editorVersion: String,
        targetPlatform: String
    ) {
        viewModelScope.launch {
            repository.updateProjectEditorAndTarget(project, editorVersion, targetPlatform)
            inputs.update {
                it.copy(
                    launchBannerMessage = "Updated ${project.name} to $editorVersion ($targetPlatform)"
                )
            }
        }
    }

    fun launchProject(project: UnityProjectEntity) {
        viewModelScope.launch {
            repository.touchProjectOpened(project)
            inputs.update {
                it.copy(
                    selectedProjectId = null,
                    launchBannerMessage = "Launching \"${project.name}\" in Unity ${project.editorVersion}..."
                )
            }
        }
    }

    fun deleteProject(project: UnityProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project.id)
            inputs.update {
                it.copy(
                    selectedProjectId = null,
                    launchBannerMessage = "Removed \"${project.name}\" from Unity Hub"
                )
            }
        }
    }

    fun dismissBanner() {
        inputs.update { it.copy(launchBannerMessage = null) }
    }

    fun updateTutorialQuery(query: String) {
        inputs.update { it.copy(tutorialQuery = query) }
    }

    fun selectTutorialCategory(category: String) {
        inputs.update { it.copy(tutorialCategory = category) }
    }

    fun openTutorialDetail(tutorial: TutorialEntity) {
        inputs.update { it.copy(selectedTutorialId = tutorial.id) }
    }

    fun closeTutorialDetail() {
        inputs.update { it.copy(selectedTutorialId = null) }
    }

    fun toggleTutorialStep(tutorial: TutorialEntity, stepIndex: Int) {
        viewModelScope.launch {
            repository.toggleTutorialStep(tutorial, stepIndex)
        }
    }

    fun toggleTutorialBookmark(tutorial: TutorialEntity) {
        viewModelScope.launch {
            repository.toggleTutorialBookmark(tutorial)
        }
    }

    fun openWebsiteUrl(url: String) {
        inputs.update {
            it.copy(
                activeTab = HubTab.WEBSITE,
                selectedTutorialId = null,
                currentWebsiteUrl = url,
                webViewReloadToken = it.webViewReloadToken + 1
            )
        }
    }

    fun reloadWebsite() {
        inputs.update { it.copy(webViewReloadToken = it.webViewReloadToken + 1) }
    }
}
