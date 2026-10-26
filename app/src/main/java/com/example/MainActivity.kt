package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.screens.*
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.StudyMateTheme
import kotlinx.coroutines.launch

data class NavItem(
    val id: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {
    private val repository = StudyMateRepository()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }
            val scope = rememberCoroutineScope()

            val currentUser by repository.currentUser.collectAsStateWithLifecycle()
            val tasks by repository.studyTasks.collectAsStateWithLifecycle()
            val posts by repository.communityPosts.collectAsStateWithLifecycle()
            val skills by repository.skillListings.collectAsStateWithLifecycle()
            val skillRequests by repository.skillRequests.collectAsStateWithLifecycle()
            val chatMessages by repository.chatMessages.collectAsStateWithLifecycle()
            val quizzes by repository.quizzes.collectAsStateWithLifecycle()
            val quizAttempts by repository.recentAttempts.collectAsStateWithLifecycle()
            val leaderboard by repository.leaderboard.collectAsStateWithLifecycle()
            val trustCheckSubmissions by repository.trustCheckSubmissions.collectAsStateWithLifecycle()
            val auditLogs by repository.auditLogs.collectAsStateWithLifecycle()

            var currentScreen by remember { mutableStateOf("dashboard") }

            val primaryNavItems = listOf(
                NavItem("dashboard", "Home", Icons.Filled.Home, Icons.Outlined.Home),
                NavItem("assistant", "Gemma AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
                NavItem("community", "Forum", Icons.Filled.Forum, Icons.Outlined.Forum),
                NavItem("skillswap", "SkillSwap", Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz),
                NavItem("quizzes", "Quizzes", Icons.Filled.Quiz, Icons.Outlined.Quiz),
                NavItem("planner", "Tasks", Icons.Filled.Checklist, Icons.Outlined.Checklist),
                NavItem("trustcheck", "TrustCheck", Icons.Filled.VerifiedUser, Icons.Outlined.VerifiedUser),
                NavItem("profile", "Settings", Icons.Filled.Person, Icons.Outlined.Person)
            )

            // Back handler pops to dashboard if on sub-screen
            BackHandler(enabled = currentScreen != "dashboard") {
                currentScreen = "dashboard"
            }

            StudyMateTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "StudyMate AI",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp,
                                        color = PrimaryIndigo
                                    )
                                    Surface(
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                        color = PrimaryIndigo.copy(alpha = 0.1f),
                                        modifier = Modifier.padding(end = 12.dp)
                                    ) {
                                        Text(
                                            text = currentUser.role.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryIndigo,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bottom_nav_bar"),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            // Display top 5 in bottom bar, others reachable via chips/dashboard
                            val bottomBarItems = listOf(
                                NavItem("dashboard", "Home", Icons.Filled.Home, Icons.Outlined.Home),
                                NavItem("assistant", "Gemma", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
                                NavItem("community", "Forum", Icons.Filled.Forum, Icons.Outlined.Forum),
                                NavItem("skillswap", "Swap", Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz),
                                NavItem("trustcheck", "Verify", Icons.Filled.VerifiedUser, Icons.Outlined.VerifiedUser)
                            )

                            bottomBarItems.forEach { item ->
                                val isSelected = currentScreen == item.id
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentScreen = item.id },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title,
                                            tint = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = PrimaryIndigo.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_${item.id}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                            when (screen) {
                                "dashboard" -> DashboardScreen(
                                    user = currentUser,
                                    tasks = tasks,
                                    posts = posts,
                                    skills = skills,
                                    savedChats = chatMessages.filter { it.isSaved },
                                    onNavigate = { currentScreen = it },
                                    onToggleTask = { repository.toggleTask(it) }
                                )
                                "assistant" -> AssistantScreen(
                                    messages = chatMessages,
                                    onSendMessage = { prompt, subj ->
                                        scope.launch {
                                            repository.askAssistant(prompt, subj)
                                        }
                                    },
                                    onToggleSave = { repository.toggleSaveChat(it) },
                                    onGenerateQuizFromTopic = {
                                        currentScreen = "quizzes"
                                    }
                                )
                                "community" -> CommunityScreen(
                                    posts = posts,
                                    onUpvote = { repository.upvotePost(it) },
                                    onCreatePost = { title, content, subject ->
                                        repository.createPost(title, content, subject)
                                    },
                                    onReportPost = { repository.reportPost(it) }
                                )
                                "skillswap" -> SkillSwapScreen(
                                    listings = skills,
                                    requests = skillRequests,
                                    onSendRequest = { toUser, teach, learn, msg ->
                                        repository.sendSkillRequest(toUser, teach, learn, msg)
                                    },
                                    onUpdateRequestStatus = { reqId, status ->
                                        repository.updateSkillRequestStatus(reqId, status)
                                    }
                                )
                                "quizzes" -> QuizzesScreen(
                                    quizzes = quizzes,
                                    attempts = quizAttempts,
                                    leaderboard = leaderboard,
                                    onSubmitScore = { title, score, total, isChall ->
                                        repository.submitQuizScore(title, score, total, isChall)
                                    }
                                )
                                "planner" -> PlannerScreen(
                                    tasks = tasks,
                                    onToggleTask = { repository.toggleTask(it) },
                                    onAddTask = { title, subject, priority, due ->
                                        repository.addTask(title, subject, priority, due)
                                    },
                                    onDeleteTask = { repository.deleteTask(it) }
                                )
                                "trustcheck" -> TrustCheckScreen(
                                    user = currentUser,
                                    submissions = trustCheckSubmissions,
                                    auditLogs = auditLogs,
                                    onSubmitVerification = { reg, docType, file ->
                                        repository.submitTrustCheck(reg, docType, file)
                                    },
                                    onReviewSubmission = { subId, approved, reason ->
                                        repository.reviewTrustCheck(subId, approved, reason)
                                    },
                                    onSwitchRole = { repository.switchUserRole(it) }
                                )
                                "profile" -> ProfileSettingsScreen(
                                    user = currentUser,
                                    isDarkTheme = isDarkTheme,
                                    onToggleDarkTheme = { isDarkTheme = it },
                                    onSwitchRole = { repository.switchUserRole(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
