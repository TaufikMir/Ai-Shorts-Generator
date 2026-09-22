package com.example.ui

import android.app.Application
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.local.CreditTransactionEntity
import com.example.data.local.ProcessingJobEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.ShortEntity
import com.example.data.local.UserAccountEntity
import com.example.data.model.PricingPlan
import com.example.data.repository.AiService
import com.example.data.repository.ShortsRepository
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.components.AppScreen
import com.example.ui.components.AppTopBar
import com.example.ui.components.OnboardingDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.BillingScreen
import com.example.ui.screens.CreateShortScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.LandingPageScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShortStudioScreen
import com.example.ui.screens.ShortsLibraryScreen
import com.example.ui.theme.BgDark
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainAppViewModel(application: Application) : AndroidViewModel(application) {
    val aiService = AiService()
    val database = AppDatabase.getInstance(application)
    val repository = ShortsRepository(database, aiService)

    val userAccount = repository.getUserAccount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val projects = repository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shorts = repository.getAllShorts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val creditTransactions = repository.getCreditTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val jobs = repository.getAllJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val creationState = repository.creationState

    fun startVideoCreation(
        fileName: String,
        fileSizeMb: Float,
        durationSec: Int,
        customTitle: String?,
        videoUri: String?
    ) {
        viewModelScope.launch {
            repository.processVideoUpload(fileName, fileSizeMb, durationSec, customTitle, videoUri)
        }
    }

    fun importYouTubeVideo(
        details: com.example.data.util.YouTubeVideoDetails,
        customTitle: String?
    ) {
        viewModelScope.launch {
            repository.processYouTubeImport(details, customTitle)
        }
    }

    fun resetCreation() {
        repository.resetCreationState()
    }

    fun cancelCreation() {
        repository.cancelCreationPipeline()
    }

    fun updateShort(short: ShortEntity) {
        viewModelScope.launch {
            repository.updateShort(short)
        }
    }

    fun toggleFavorite(shortId: Long, current: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(shortId, current)
        }
    }

    fun deleteShort(shortId: Long) {
        viewModelScope.launch {
            repository.deleteShort(shortId)
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
        }
    }

    fun getTranscriptByProjectId(projectId: Long): kotlinx.coroutines.flow.Flow<com.example.data.local.TranscriptEntity?> =
        repository.getTranscriptByProjectId(projectId)

    fun retranscribeProject(projectId: Long) {
        viewModelScope.launch {
            repository.retranscribeProject(projectId)
        }
    }

    fun upgradePlan(plan: PricingPlan) {
        viewModelScope.launch {
            repository.upgradePlan(plan)
        }
    }

    fun addAdminCredits(amount: Int) {
        viewModelScope.launch {
            repository.addCreditsAdmin(1, amount, "Manual top-up")
        }
    }

    fun saveProfile(name: String, email: String, category: String, platforms: String, apiKey: String) {
        viewModelScope.launch {
            repository.updateUserProfile(name, email, category, platforms, apiKey)
        }
    }

    fun completeOnboarding(category: String, platforms: String) {
        viewModelScope.launch {
            repository.completeOnboarding(category, platforms)
        }
    }
}

@Composable
fun MainApp(
    viewModel: MainAppViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val userAccount by viewModel.userAccount.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val shorts by viewModel.shorts.collectAsState()
    val creditTransactions by viewModel.creditTransactions.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    val creationState by viewModel.creationState.collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var activeStudioShortId by remember { mutableStateOf<Long?>(null) }
    var activeExportShortId by remember { mutableStateOf<Long?>(null) }
    var showLandingPage by remember { mutableStateOf(false) }
    var showAdminDashboard by remember { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf(false) }

    // Intercept back button when sub-screens are open
    BackHandler(enabled = activeStudioShortId != null || activeExportShortId != null || showAdminDashboard || showLandingPage) {
        when {
            activeExportShortId != null -> activeExportShortId = null
            activeStudioShortId != null -> activeStudioShortId = null
            showAdminDashboard -> showAdminDashboard = false
            showLandingPage -> showLandingPage = false
        }
    }

    // Full screen Overlay views
    when {
        showLandingPage -> {
            LandingPageScreen(
                onCloseLanding = { showLandingPage = false },
                onGetStarted = {
                    showLandingPage = false
                    currentScreen = AppScreen.CREATE
                }
            )
            return
        }

        showAdminDashboard -> {
            AdminDashboardScreen(
                jobs = jobs,
                onCloseAdmin = { showAdminDashboard = false },
                onAddCredits = { amount -> viewModel.addAdminCredits(amount) }
            )
            return
        }

        activeExportShortId != null -> {
            val selectedShort = shorts.find { it.id == activeExportShortId }
            if (selectedShort != null) {
                ExportScreen(
                    short = selectedShort,
                    onBack = { activeExportShortId = null },
                    onSaveShort = { updated -> viewModel.updateShort(updated) },
                    onExportRender = { id, onProgress ->
                        viewModel.repository.exportShort(id, onProgress)
                    },
                    onOpenStudio = {
                        val sid = selectedShort.id
                        activeExportShortId = null
                        activeStudioShortId = sid
                    }
                )
                return
            } else {
                activeExportShortId = null
            }
        }

        activeStudioShortId != null -> {
            val selectedShort = shorts.find { it.id == activeStudioShortId }
            if (selectedShort != null) {
                ShortStudioScreen(
                    short = selectedShort,
                    aiService = viewModel.aiService,
                    onBack = { activeStudioShortId = null },
                    onSaveShort = { updated -> viewModel.updateShort(updated) },
                    onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) },
                    onExportRender = { id, onProgress ->
                        viewModel.repository.exportShort(id, onProgress)
                    },
                    getTranscriptByProjectId = { viewModel.getTranscriptByProjectId(it) },
                    onRetranscribe = { viewModel.retranscribeProject(it) },
                    onOpenExportScreen = { id ->
                        activeStudioShortId = null
                        activeExportShortId = id
                    }
                )
                return
            } else {
                activeStudioShortId = null
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgDark,
        topBar = {
            AppTopBar(
                currentCreditsUsed = userAccount?.creditsUsed ?: 37,
                totalCredits = userAccount?.creditsTotal ?: 100,
                planName = userAccount?.planId ?: "Creator",
                onNavigateToCreate = { currentScreen = AppScreen.CREATE },
                onNavigateToBilling = { currentScreen = AppScreen.BILLING },
                onToggleLandingPage = { showLandingPage = !showLandingPage },
                onToggleAdmin = { showAdminDashboard = !showAdminDashboard },
                isAdminVisible = showAdminDashboard,
                isLandingVisible = showLandingPage
            )
        },
        bottomBar = {
            AppBottomNavigationBar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> {
                    DashboardScreen(
                        userAccount = userAccount,
                        projects = projects,
                        shorts = shorts,
                        onNavigateToCreate = { currentScreen = AppScreen.CREATE },
                        onNavigateToProjects = { currentScreen = AppScreen.PROJECTS },
                        onNavigateToShorts = { currentScreen = AppScreen.SHORTS },
                        onOpenShortStudio = { shortId -> activeStudioShortId = shortId },
                        onDeleteProject = { id -> viewModel.deleteProject(id) },
                        onOpenExport = { shortId -> activeExportShortId = shortId }
                    )
                }

                AppScreen.CREATE -> {
                    CreateShortScreen(
                        pipelineStatus = creationState,
                        onStartUploadAndProcess = { name, size, duration, title, uri ->
                            viewModel.startVideoCreation(name, size, duration, title, uri)
                        },
                        onImportYouTubeVideo = { details, title ->
                            viewModel.importYouTubeVideo(details, title)
                        },
                        onResetPipeline = { viewModel.resetCreation() },
                        onCancelUpload = { viewModel.cancelCreation() },
                        onViewGeneratedProject = { projectId ->
                            val firstShort = shorts.firstOrNull { it.projectId == projectId }
                            if (firstShort != null) {
                                activeStudioShortId = firstShort.id
                            } else {
                                currentScreen = AppScreen.SHORTS
                            }
                        }
                    )
                }

                AppScreen.PROJECTS -> {
                    ProjectsScreen(
                        projects = projects,
                        onCreateNewProject = { currentScreen = AppScreen.CREATE },
                        onOpenProject = { projectId ->
                            val projectShort = shorts.firstOrNull { it.projectId == projectId }
                            if (projectShort != null) {
                                activeStudioShortId = projectShort.id
                            } else {
                                currentScreen = AppScreen.SHORTS
                            }
                        },
                        onDeleteProject = { id -> viewModel.deleteProject(id) }
                    )
                }

                AppScreen.SHORTS -> {
                    ShortsLibraryScreen(
                        shorts = shorts,
                        onOpenShortStudio = { shortId -> activeStudioShortId = shortId },
                        onDeleteShort = { id -> viewModel.deleteShort(id) },
                        onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) },
                        onExportQuick = { shortId -> activeExportShortId = shortId },
                        onOpenExport = { shortId -> activeExportShortId = shortId }
                    )
                }

                AppScreen.BILLING -> {
                    BillingScreen(
                        userAccount = userAccount,
                        transactions = creditTransactions,
                        onUpgradePlan = { plan -> viewModel.upgradePlan(plan) }
                    )
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        userAccount = userAccount,
                        onSaveProfile = { name: String, email: String, category: String, platforms: String, apiKey: String ->
                            viewModel.saveProfile(name, email, category, platforms, apiKey)
                        },
                        onOpenOnboarding = { showOnboarding = true }
                    )
                }
            }
        }
    }

    if (showOnboarding) {
        OnboardingDialog(
            initialCategory = userAccount?.contentCategory ?: "PODCAST",
            initialPlatform = userAccount?.publishPlatforms ?: "MULTIPLE",
            onComplete = { cat, plat ->
                viewModel.completeOnboarding(cat, plat)
                showOnboarding = false
                Toast.makeText(context, "Personalization updated!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showOnboarding = false }
        )
    }
}
