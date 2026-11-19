package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.UnityWebUrls
import com.example.ui.components.CreateProjectDialog
import com.example.ui.components.ProjectDetailSheet
import com.example.ui.components.UnityHubToolbar
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.TutorialsScreen
import com.example.ui.screens.WebsiteScreen
import com.example.ui.theme.UnityHubTheme
import com.example.ui.viewmodel.HubTab
import com.example.ui.viewmodel.HubUiState
import com.example.ui.viewmodel.UnityHubViewModel

const val ROUTE_PROJECTS = "projects"
const val ROUTE_TUTORIALS = "tutorials"
const val ROUTE_WEBSITE = "website"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnityHubTheme {
                UnityHubApp()
            }
        }
    }
}

private data class NavDestinationItem(
    val tab: HubTab,
    val route: String,
    val labelResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val hubNavItems = listOf(
    NavDestinationItem(
        tab = HubTab.PROJECTS,
        route = ROUTE_PROJECTS,
        labelResId = R.string.tab_projects,
        selectedIcon = Icons.Filled.Folder,
        unselectedIcon = Icons.Outlined.Folder,
        testTag = "nav_tab_projects"
    ),
    NavDestinationItem(
        tab = HubTab.TUTORIALS,
        route = ROUTE_TUTORIALS,
        labelResId = R.string.tab_tutorials,
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School,
        testTag = "nav_tab_tutorials"
    ),
    NavDestinationItem(
        tab = HubTab.WEBSITE,
        route = ROUTE_WEBSITE,
        labelResId = R.string.tab_website,
        selectedIcon = Icons.Filled.Language,
        unselectedIcon = Icons.Outlined.Language,
        testTag = "nav_tab_website"
    )
)

@Composable
fun UnityHubApp(
    viewModel: UnityHubViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_PROJECTS

    val activeTab = when (currentRoute) {
        ROUTE_TUTORIALS -> HubTab.TUTORIALS
        ROUTE_WEBSITE -> HubTab.WEBSITE
        else -> HubTab.PROJECTS
    }

    val navigateToTab: (HubTab) -> Unit = { tab ->
        val route = when (tab) {
            HubTab.PROJECTS -> ROUTE_PROJECTS
            HubTab.TUTORIALS -> ROUTE_TUTORIALS
            HubTab.WEBSITE -> ROUTE_WEBSITE
        }
        viewModel.selectTab(tab)
        navigateSingleTop(navController, route)
    }

    val openWebsite: (String) -> Unit = { url ->
        viewModel.openWebsiteUrl(url)
        navigateSingleTop(navController, ROUTE_WEBSITE)
    }

    val readyState = uiState as? HubUiState.Ready
    val showTutorialBackButton = activeTab == HubTab.TUTORIALS && readyState?.selectedTutorial != null

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                UnityHubToolbar(
                    activeTab = activeTab,
                    showBackButton = showTutorialBackButton,
                    onBackClick = { viewModel.closeTutorialDetail() },
                    onQuickAddProjectClick = { viewModel.setCreateProjectDialogOpen(true) },
                    onQuickWebsiteClick = { openWebsite(UnityWebUrls.BRANDING_PT) }
                )
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        hubNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(item.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(stringResource(item.labelResId)) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (activeTab == HubTab.PROJECTS && readyState != null) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.setCreateProjectDialogOpen(true) },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(R.string.cd_add_project)
                            )
                        },
                        text = { Text(stringResource(R.string.new_project)) },
                        modifier = Modifier.testTag("fab_new_project")
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        hubNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(item.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(stringResource(item.labelResId)) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                when (val state = uiState) {
                    HubUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is HubUiState.Ready -> {
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_PROJECTS,
                            modifier = Modifier.weight(1f)
                        ) {
                            composable(ROUTE_PROJECTS) {
                                ProjectsScreen(
                                    projects = state.projects,
                                    allProjectCount = state.allProjectCount,
                                    searchQuery = state.projectQuery,
                                    selectedFilter = state.selectedProjectFilter,
                                    bannerMessage = state.launchBannerMessage,
                                    onSearchQueryChange = viewModel::updateProjectQuery,
                                    onSelectFilter = viewModel::selectProjectFilter,
                                    onProjectClick = viewModel::openProjectDetail,
                                    onToggleFavorite = viewModel::toggleProjectFavorite,
                                    onLaunchProject = viewModel::launchProject,
                                    onCreateProjectClick = { viewModel.setCreateProjectDialogOpen(true) },
                                    onDismissBanner = viewModel::dismissBanner,
                                    onOpenBrandingSite = { openWebsite(UnityWebUrls.BRANDING_PT) }
                                )
                            }

                            composable(ROUTE_TUTORIALS) {
                                TutorialsScreen(
                                    tutorials = state.tutorials,
                                    searchQuery = state.tutorialQuery,
                                    selectedCategory = state.selectedTutorialCategory,
                                    selectedTutorial = state.selectedTutorial,
                                    onSearchQueryChange = viewModel::updateTutorialQuery,
                                    onSelectCategory = viewModel::selectTutorialCategory,
                                    onTutorialClick = viewModel::openTutorialDetail,
                                    onCloseTutorialDetail = viewModel::closeTutorialDetail,
                                    onToggleTutorialStep = viewModel::toggleTutorialStep,
                                    onToggleBookmark = viewModel::toggleTutorialBookmark,
                                    onOpenWebsiteUrl = openWebsite
                                )
                            }

                            composable(ROUTE_WEBSITE) {
                                WebsiteScreen(
                                    currentUrl = state.currentWebsiteUrl,
                                    reloadToken = state.webViewReloadToken,
                                    onSelectUrl = { url -> openWebsite(url) },
                                    onReload = viewModel::reloadWebsite
                                )
                            }
                        }

                        if (state.isCreateProjectDialogOpen) {
                            CreateProjectDialog(
                                onDismiss = { viewModel.setCreateProjectDialogOpen(false) },
                                onCreate = viewModel::createProject
                            )
                        }

                        state.selectedProject?.let { project ->
                            ProjectDetailSheet(
                                project = project,
                                onDismiss = viewModel::closeProjectDetail,
                                onToggleFavorite = viewModel::toggleProjectFavorite,
                                onSaveConfig = viewModel::updateProjectConfig,
                                onLaunchProject = viewModel::launchProject,
                                onDeleteProject = { toDelete ->
                                    viewModel.deleteProject(toDelete)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun navigateSingleTop(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
