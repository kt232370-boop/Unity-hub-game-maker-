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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.remote.UserProfileRepository
import com.example.data.repository.UnityWebUrls
import com.example.ui.auth.AuthFlowScreen
import com.example.ui.auth.PendingUnityIdDraft
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOut
import com.example.ui.components.CreateProjectDialog
import com.example.ui.components.ProjectDetailSheet
import com.example.ui.components.UnityAccountSheet
import com.example.ui.components.UnityHubToolbar
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.TutorialsScreen
import com.example.ui.screens.WebsiteScreen
import com.example.ui.theme.UnityHubTheme
import com.example.ui.viewmodel.AccountViewModel
import com.example.ui.viewmodel.HubTab
import com.example.ui.viewmodel.HubUiState
import com.example.ui.viewmodel.ProfileUiState
import com.example.ui.viewmodel.UnityHubViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

const val ROUTE_PROJECTS = "projects"
const val ROUTE_TUTORIALS = "tutorials"
const val ROUTE_WEBSITE = "website"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnityHubTheme {
                AppNavigation()
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
fun AppNavigation() {
    val auth = remember { Firebase.auth }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var pendingDraft by remember { mutableStateOf<PendingUnityIdDraft?>(null) }
    var checkedAutoSignIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!checkedAutoSignIn) {
            checkedAutoSignIn = true
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthFlowScreen(
            onGoogleAuthComplete = { draft ->
                pendingDraft = draft
            }
        )
    } else {
        key(user.uid) {
            UnityHubApp(
                currentUser = user,
                pendingDraft = pendingDraft,
                onConsumeDraft = { pendingDraft = null }
            )
        }
    }
}

@Composable
fun UnityHubApp(
    currentUser: FirebaseUser,
    pendingDraft: PendingUnityIdDraft?,
    onConsumeDraft: () -> Unit,
    viewModel: UnityHubViewModel = viewModel(key = currentUser.uid)
) {
    val currentUserId = currentUser.uid
    val accountViewModel: AccountViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                AccountViewModel(UserProfileRepository(db), currentUserId)
            }
        }
    )

    LaunchedEffect(currentUserId, pendingDraft) {
        accountViewModel.syncSignedInProfile(pendingDraft)
        if (pendingDraft != null) {
            onConsumeDraft()
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profileState by accountViewModel.profileState.collectAsStateWithLifecycle()
    val syncError by accountViewModel.syncError.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isAccountSheetOpen by remember { mutableStateOf(false) }

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
                    onQuickWebsiteClick = { openWebsite(UnityWebUrls.BRANDING_PT) },
                    onAccountClick = { isAccountSheetOpen = true }
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

    if (isAccountSheetOpen) {
        val loadedProfile = (profileState as? ProfileUiState.Ready)?.profile
        val profileError = (profileState as? ProfileUiState.Error)?.message ?: syncError
        UnityAccountSheet(
            profile = loadedProfile,
            fallbackEmail = currentUser.email,
            fallbackName = currentUser.displayName,
            syncError = profileError,
            onDismiss = {
                accountViewModel.clearSyncError()
                isAccountSheetOpen = false
            },
            onSignOutClick = {
                isAccountSheetOpen = false
                signOut(credentialManager = credentialManager, scope = scope)
            }
        )
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
