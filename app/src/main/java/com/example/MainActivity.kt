package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LearnovaTopBar
import com.example.ui.components.WhatShouldIDoDialog
import com.example.ui.screens.*
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.LearnovaTheme
import com.example.ui.viewmodel.LearnovaViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LearnovaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            val subjects by viewModel.subjects.collectAsStateWithLifecycle()
            val chapters by viewModel.chapters.collectAsStateWithLifecycle()
            val topics by viewModel.topics.collectAsStateWithLifecycle()
            val exams by viewModel.exams.collectAsStateWithLifecycle()
            val studyTasks by viewModel.studyTasks.collectAsStateWithLifecycle()
            val focusSessions by viewModel.focusSessions.collectAsStateWithLifecycle()
            val mistakes by viewModel.mistakes.collectAsStateWithLifecycle()
            val resources by viewModel.resources.collectAsStateWithLifecycle()
            val achievements by viewModel.achievements.collectAsStateWithLifecycle()
            val widgetPreferences by viewModel.widgetPreferences.collectAsStateWithLifecycle()

            val nextBestAction by viewModel.nextBestAction.collectAsStateWithLifecycle()
            val readinessBreakdown by viewModel.readinessBreakdown.collectAsStateWithLifecycle()
            val dailyBriefing by viewModel.dailyBriefing.collectAsStateWithLifecycle()
            val todayStudyMinutes by viewModel.todayStudyMinutes.collectAsStateWithLifecycle()
            val weakTopics by viewModel.weakTopics.collectAsStateWithLifecycle()
            val revisionDueTopics by viewModel.revisionDueTopics.collectAsStateWithLifecycle()

            // Timer States
            val timerRunning by viewModel.timerRunning.collectAsStateWithLifecycle()
            val timerRemainingSeconds by viewModel.timerRemainingSeconds.collectAsStateWithLifecycle()
            val timerTotalSeconds by viewModel.timerTotalSeconds.collectAsStateWithLifecycle()
            val timerMode by viewModel.timerMode.collectAsStateWithLifecycle()
            val activeSubject by viewModel.activeSubject.collectAsStateWithLifecycle()
            val activeTopic by viewModel.activeTopic.collectAsStateWithLifecycle()
            val showSessionCompleteDialog by viewModel.showSessionCompleteDialog.collectAsStateWithLifecycle()

            // Chat States
            val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
            val isCoachThinking by viewModel.isCoachThinking.collectAsStateWithLifecycle()

            val replanMessage by viewModel.replanMessage.collectAsStateWithLifecycle()

            // App State
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            // Sync with profile theme if set
            LaunchedEffect(userProfile?.themeMode) {
                userProfile?.themeMode?.let { mode ->
                    when (mode) {
                        "Dark" -> isDarkTheme = true
                        "Light" -> isDarkTheme = false
                        else -> isDarkTheme = systemDark
                    }
                }
            }

            var currentTabIndex by remember { mutableStateOf(0) }
            var showWhatShouldIDoDialog by remember { mutableStateOf(false) }
            var showProfileDialog by remember { mutableStateOf(false) }
            var showResetOnboarding by remember { mutableStateOf(false) }

            LearnovaTheme(darkTheme = isDarkTheme) {
                if (userProfile?.isOnboarded == false || showResetOnboarding) {
                    OnboardingScreen(
                        onComplete = { name, grade, curr, targetMins, exName, exDays, goal ->
                            showResetOnboarding = false
                            viewModel.completeOnboarding(
                                name, grade, curr, targetMins, exName, exDays, goal
                            )
                        }
                    )
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isWideScreen = maxWidth >= 600.dp

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                LearnovaTopBar(
                                    onWhatShouldIDoClick = { showWhatShouldIDoDialog = true },
                                    onProfileClick = { showProfileDialog = true },
                                    darkTheme = isDarkTheme,
                                    onToggleTheme = {
                                        isDarkTheme = !isDarkTheme
                                        viewModel.updateTheme(if (isDarkTheme) "Dark" else "Light")
                                    }
                                )
                            },
                            bottomBar = {
                                if (!isWideScreen) {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.testTag("bottom_navigation_bar")
                                    ) {
                                        val navItems = listOf(
                                            Triple(0, "Home", Icons.Default.Home),
                                            Triple(1, "Syllabus", Icons.Default.MenuBook),
                                            Triple(2, "Focus", Icons.Default.Timer),
                                            Triple(3, "Mistakes", Icons.Default.ReportProblem),
                                            Triple(4, "Resources", Icons.Default.LibraryBooks),
                                            Triple(5, "Insights", Icons.Default.Analytics),
                                            Triple(6, "Coach", Icons.Default.SmartToy)
                                        )

                                        navItems.forEach { (index, title, icon) ->
                                            val isSelected = currentTabIndex == index
                                            NavigationBarItem(
                                                selected = isSelected,
                                                onClick = { currentTabIndex = index },
                                                icon = {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = title
                                                    )
                                                },
                                                label = { Text(title, style = MaterialTheme.typography.labelSmall) },
                                                alwaysShowLabel = false,
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = BrandIndigo,
                                                    selectedTextColor = BrandIndigo,
                                                    indicatorColor = BrandIndigo.copy(alpha = 0.12f)
                                                ),
                                                modifier = Modifier.testTag("nav_item_$index")
                                            )
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                if (isWideScreen) {
                                    NavigationRail(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ) {
                                        val navItems = listOf(
                                            Triple(0, "Home", Icons.Default.Home),
                                            Triple(1, "Syllabus", Icons.Default.MenuBook),
                                            Triple(2, "Focus", Icons.Default.Timer),
                                            Triple(3, "Mistakes", Icons.Default.ReportProblem),
                                            Triple(4, "Resources", Icons.Default.LibraryBooks),
                                            Triple(5, "Insights", Icons.Default.Analytics),
                                            Triple(6, "Coach", Icons.Default.SmartToy)
                                        )

                                        navItems.forEach { (index, title, icon) ->
                                            NavigationRailItem(
                                                selected = currentTabIndex == index,
                                                onClick = { currentTabIndex = index },
                                                icon = { Icon(imageVector = icon, contentDescription = title) },
                                                label = { Text(title) }
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    when (currentTabIndex) {
                                        0 -> HomeScreen(
                                            profile = userProfile,
                                            dailyBriefing = dailyBriefing,
                                            nextBestAction = nextBestAction,
                                            todayStudyMinutes = todayStudyMinutes,
                                            exams = exams,
                                            tasks = studyTasks,
                                            weakTopics = weakTopics,
                                            revisionDueTopics = revisionDueTopics,
                                            widgetPreferences = widgetPreferences,
                                            mistakes = mistakes,
                                            replanMessage = replanMessage,
                                            onDismissReplanMessage = { viewModel.dismissReplanMessage() },
                                            onStartFocusSession = { subject, topic ->
                                                viewModel.selectTopicForTimer(subject, topic)
                                                currentTabIndex = 2 // Switch to Focus Timer
                                                viewModel.startTimer()
                                            },
                                            onToggleTask = { task -> viewModel.toggleTask(task) },
                                            onNavigateToTab = { tab -> currentTabIndex = tab },
                                            onTriggerReplanning = { mins -> viewModel.triggerReplanning(mins) },
                                            onAddTask = { s, c, t, act, d, p -> viewModel.addStudyTask(s, c, t, act, d, p) },
                                            onToggleWidget = { pref -> viewModel.toggleWidget(pref) },
                                            onResetWidgets = { viewModel.resetWidgets() }
                                        )
                                        1 -> SyllabusScreen(
                                            subjects = subjects,
                                            chapters = chapters,
                                            topics = topics,
                                            onAddSubject = { name, color, hours -> viewModel.addSubject(name, color, hours) },
                                            onDeleteSubject = { id -> viewModel.deleteSubject(id) },
                                            onAddChapter = { subId, title -> viewModel.addChapter(subId, title) },
                                            onDeleteChapter = { id -> viewModel.deleteChapter(id) },
                                            onAddTopic = { chId, subId, title, mins -> viewModel.addTopic(chId, subId, title, mins) },
                                            onUpdateTopic = { topic, st, acc, conf -> viewModel.updateTopicStatus(topic, st, acc, conf) },
                                            onDeleteTopic = { id -> viewModel.deleteTopic(id) },
                                            onStartFocusSession = { subject, topic ->
                                                viewModel.selectTopicForTimer(subject, topic)
                                                currentTabIndex = 2
                                                viewModel.startTimer()
                                            }
                                        )
                                        2 -> FocusScreen(
                                            timerRunning = timerRunning,
                                            timerRemainingSeconds = timerRemainingSeconds,
                                            timerTotalSeconds = timerTotalSeconds,
                                            timerMode = timerMode,
                                            activeSubject = activeSubject,
                                            activeTopic = activeTopic,
                                            sessions = focusSessions,
                                            showSessionCompleteDialog = showSessionCompleteDialog,
                                            onStartTimer = { viewModel.startTimer() },
                                            onPauseTimer = { viewModel.pauseTimer() },
                                            onResetTimer = { viewModel.resetTimer() },
                                            onSetDuration = { mins, mode -> viewModel.setTimerDurationMinutes(mins, mode) },
                                            onDismissDialog = { viewModel.dismissCompleteDialog() },
                                            onSaveSession = { rating, notes -> viewModel.saveCompletedSession(rating, notes) }
                                        )
                                        3 -> MistakeBankScreen(
                                            mistakes = mistakes,
                                            exams = exams,
                                            onAddMistake = { q, sAns, cAns, exp, sub, ch, top, type ->
                                                viewModel.addMistake(q, sAns, cAns, exp, sub, ch, top, type)
                                            },
                                            onUpdateMistakeStatus = { mis, newSt -> viewModel.updateMistakeStatus(mis, newSt) },
                                            onDeleteMistake = { id -> viewModel.deleteMistake(id) },
                                            onStartFocusSession = { subject, topic ->
                                                viewModel.selectTopicForTimer(subject, topic)
                                                currentTabIndex = 2
                                                viewModel.startTimer()
                                            }
                                        )
                                        4 -> ResourcesScreen(
                                            resources = resources,
                                            onToggleSaved = { res -> viewModel.toggleResourceSaved(res) },
                                            onUpdateProgress = { res, newSt -> viewModel.updateResourceProgress(res, newSt) },
                                            onAddToPlan = { res -> viewModel.addResourceToPlan(res) }
                                        )
                                        5 -> InsightsScreen(
                                            readiness = readinessBreakdown,
                                            sessions = focusSessions,
                                            mistakes = mistakes,
                                            todayStudyMinutes = todayStudyMinutes,
                                            onAddMistake = { q, sAns, cAns, exp, sub, ch, top, type ->
                                                viewModel.addMistake(q, sAns, cAns, exp, sub, ch, top, type)
                                            },
                                            onUpdateMistakeStatus = { mis, newSt -> viewModel.updateMistakeStatus(mis, newSt) },
                                            onDeleteMistake = { id -> viewModel.deleteMistake(id) }
                                        )
                                        6 -> AiCoachScreen(
                                            messages = chatMessages,
                                            isThinking = isCoachThinking,
                                            onSendMessage = { prompt -> viewModel.sendChatMessage(prompt) }
                                        )
                                    }
                                }
                            }
                        }

                        // Dialogs
                        if (showWhatShouldIDoDialog) {
                            WhatShouldIDoDialog(
                                action = nextBestAction,
                                onDismiss = { showWhatShouldIDoDialog = false },
                                onStartAction = { subject, topic ->
                                    viewModel.selectTopicForTimer(subject, topic)
                                    currentTabIndex = 2
                                    viewModel.startTimer()
                                }
                            )
                        }

                        if (showProfileDialog) {
                            ProfileDialog(
                                profile = userProfile,
                                achievements = achievements,
                                onDismiss = { showProfileDialog = false },
                                onUpdateDailyTarget = { newMins ->
                                    val current = userProfile
                                    if (current != null) {
                                        viewModel.completeOnboarding(
                                            current.name,
                                            current.grade,
                                            current.curriculum,
                                            newMins,
                                            "",
                                            45,
                                            current.academicGoal
                                        )
                                    }
                                },
                                onResetOnboarding = {
                                    showProfileDialog = false
                                    showResetOnboarding = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
