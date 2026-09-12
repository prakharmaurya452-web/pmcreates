package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.DailyBriefing
import com.example.data.ai.NextBestAction
import com.example.data.model.Exam
import com.example.data.model.Mistake
import com.example.data.model.StudyTask
import com.example.data.model.Topic
import com.example.data.model.UserProfile
import com.example.data.model.WidgetPreference
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    profile: UserProfile?,
    dailyBriefing: DailyBriefing?,
    nextBestAction: NextBestAction?,
    todayStudyMinutes: Int,
    exams: List<Exam>,
    tasks: List<StudyTask>,
    weakTopics: List<Topic>,
    revisionDueTopics: List<Topic>,
    widgetPreferences: List<WidgetPreference>,
    mistakes: List<Mistake> = emptyList(),
    replanMessage: String?,
    onDismissReplanMessage: () -> Unit,
    onStartFocusSession: (subject: String, topic: String) -> Unit,
    onToggleTask: (StudyTask) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onTriggerReplanning: (availableMinutes: Int) -> Unit,
    onAddTask: (subject: String, chapter: String, topic: String, type: String, duration: Int, priority: String) -> Unit,
    onToggleWidget: (WidgetPreference) -> Unit,
    onResetWidgets: () -> Unit
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showReplanDialog by remember { mutableStateOf(false) }
    var showCustomizeWidgetsDialog by remember { mutableStateOf(false) }

    val targetMinutes = profile?.availableMinutesPerDay ?: 150
    val remainingMinutes = (targetMinutes - todayStudyMinutes).coerceAtLeast(0)
    val targetProgress = (todayStudyMinutes.toFloat() / targetMinutes.toFloat()).coerceIn(0f, 1f)

    val nearestExam = exams.minByOrNull { it.examDateTimestamp }
    val daysToExam = if (nearestExam != null) {
        ((nearestExam.examDateTimestamp - System.currentTimeMillis()) / 86400000L).toInt().coerceAtLeast(1)
    } else 45

    val isWidgetVisible: (String) -> Boolean = { key ->
        widgetPreferences.find { it.widgetKey == key }?.isVisible ?: true
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = BrandIndigo,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Replanning notification toast/card if active
            if (replanMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandEmerald.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = BrandEmerald)
                                Text(
                                    text = replanMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = onDismissReplanMessage) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 1. Daily Briefing Banner
            if (dailyBriefing != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = BrandIndigoLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "DAILY STUDY BRIEF",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = BrandIndigoLight
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BrandIndigo.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${dailyBriefing.nearestExamDays}d to Board",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = BrandIndigoLight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "🎯 Priority: ${dailyBriefing.topPriority}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "\"${dailyBriefing.motivationalQuote}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Today's Study Target
            if (isWidgetVisible("widget_today_target")) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth().testTag("today_target_widget")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Today's Study Target",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${todayStudyMinutes}m of ${targetMinutes}m completed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(
                                    onClick = { showReplanDialog = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Replan")
                                }
                            }

                            LinearProgressIndicator(
                                progress = targetProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = BrandIndigo,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StudyMetricPill("Completed", "${todayStudyMinutes}m", BrandEmerald)
                                StudyMetricPill("Remaining", "${remainingMinutes}m", BrandAmber)
                                StudyMetricPill("Streak", "${profile?.currentStreak ?: 3} days 🔥", BrandRose)
                            }
                        }
                    }
                }
            }

            // 3. NEXT BEST ACTION Card (Learnova's Signature Feature!)
            if (isWidgetVisible("widget_next_best_action") && nextBestAction != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandIndigo.copy(alpha = 0.08f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, BrandIndigo.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().testTag("next_best_action_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = BrandIndigo, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = "NEXT BEST ACTION",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = BrandIndigo
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BrandRose.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = nextBestAction.priority,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = BrandRose,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${nextBestAction.subject} • ${nextBestAction.durationMinutes} min",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = nextBestAction.topic,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = nextBestAction.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = {
                                    onStartFocusSession(nextBestAction.subject, nextBestAction.topic)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                                modifier = Modifier.fillMaxWidth().testTag("start_now_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Now (${nextBestAction.durationMinutes} min)")
                            }
                        }
                    }
                }
            }

            // 4. Quick Actions Grid
            if (isWidgetVisible("widget_quick_actions")) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Quick Actions",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickActionButton(
                                icon = Icons.Default.Timer,
                                label = "Focus",
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTab(2) }
                            )
                            QuickActionButton(
                                icon = Icons.Default.ReportProblem,
                                label = "Mistakes",
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTab(3) }
                            )
                            QuickActionButton(
                                icon = Icons.Default.AddTask,
                                label = "Add Task",
                                modifier = Modifier.weight(1f),
                                onClick = { showAddTaskDialog = true }
                            )
                            QuickActionButton(
                                icon = Icons.Default.LibraryBooks,
                                label = "Resources",
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTab(4) }
                            )
                        }
                    }
                }
            }

            // 5. Nearest Exam Countdown
            if (isWidgetVisible("widget_exam_countdown") && nearestExam != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth().testTag("exam_countdown_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = nearestExam.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Target Score: ${nearestExam.targetScore}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BrandEmerald
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BrandCyan.copy(alpha = 0.15f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$daysToExam",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                            color = BrandCyan
                                        )
                                        Text(
                                            text = "DAYS LEFT",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = BrandCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Revision Due Today
            if (isWidgetVisible("widget_revision_due") && revisionDueTopics.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth().testTag("revision_due_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Update, contentDescription = null, tint = BrandViolet)
                                    Text(
                                        text = "Revision Due Today (${revisionDueTopics.size})",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                TextButton(onClick = { onNavigateToTab(1) }) {
                                    Text("View All")
                                }
                            }

                            revisionDueTopics.take(2).forEach { topic ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = topic.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Accuracy: ${topic.accuracyPercent}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val subName = if (topic.subjectId.contains("math")) "Mathematics" else "Science"
                                            onStartFocusSession(subName, topic.title)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Revise")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6b. Mistake Bank Widget
            if (isWidgetVisible("widget_mistakes")) {
                val unresolvedMistakes = mistakes.filter { it.status == "Unresolved" }
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTab(3) }
                            .testTag("home_mistake_bank_widget")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ReportProblem,
                                        contentDescription = null,
                                        tint = BrandRose,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Mistake Bank (${unresolvedMistakes.size} Unresolved)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                TextButton(
                                    onClick = { onNavigateToTab(3) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Open Bank", style = MaterialTheme.typography.labelMedium, color = BrandIndigo)
                                }
                            }

                            if (unresolvedMistakes.isEmpty()) {
                                Text(
                                    text = "All clear! No pending unresolved mistakes logged.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrandEmerald
                                )
                            } else {
                                unresolvedMistakes.take(2).forEach { mistake ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BrandRose.copy(alpha = 0.06f))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = mistake.question,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${mistake.subject} • ${mistake.mistakeType} Error",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { onStartFocusSession(mistake.subject, mistake.topic) },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Fix", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Today's Scheduled Tasks
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Study Tasks (${tasks.count { it.isCompleted }}/${tasks.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { showCustomizeWidgetsDialog = true }) {
                        Icon(Icons.Default.DashboardCustomize, contentDescription = "Customize Widgets", modifier = Modifier.size(20.dp))
                    }
                }
            }

            items(tasks) { task ->
                StudyTaskItem(
                    task = task,
                    onToggle = { onToggleTask(task) },
                    onStartFocus = { onStartFocusSession(task.subject, task.topic) }
                )
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { subject, chapter, topic, type, duration, priority ->
                onAddTask(subject, chapter, topic, type, duration, priority)
                showAddTaskDialog = false
            }
        )
    }

    // Replan Day Dialog
    if (showReplanDialog) {
        ReplanDialog(
            currentMinutes = targetMinutes,
            onDismiss = { showReplanDialog = false },
            onConfirm = { newMinutes ->
                onTriggerReplanning(newMinutes)
                showReplanDialog = false
            }
        )
    }

    // Customize Widgets Dialog
    if (showCustomizeWidgetsDialog) {
        CustomizeWidgetsDialog(
            widgets = widgetPreferences,
            onToggle = onToggleWidget,
            onReset = onResetWidgets,
            onDismiss = { showCustomizeWidgetsDialog = false }
        )
    }
}

@Composable
private fun StudyMetricPill(label: String, value: String, accentColor: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = accentColor.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = BrandIndigo, modifier = Modifier.size(22.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StudyTaskItem(
    task: StudyTask,
    onToggle: () -> Unit,
    onStartFocus: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${task.subject} • ${task.durationMinutes}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = task.topic,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (task.reason.isNotBlank()) {
                    Text(
                        text = task.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (!task.isCompleted) {
                IconButton(onClick = onStartFocus) {
                    Icon(Icons.Default.PlayCircle, contentDescription = "Start Focus", tint = BrandIndigo)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (subject: String, chapter: String, topic: String, type: String, duration: Int, priority: String) -> Unit
) {
    var subject by remember { mutableStateOf("Mathematics") }
    var chapter by remember { mutableStateOf("Quadratic Equations") }
    var topic by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Practice") }
    var duration by remember { mutableStateOf("30") }
    var priority by remember { mutableStateOf("High") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Study Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration (min)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = priority,
                        onValueChange = { priority = it },
                        label = { Text("Priority") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topic.isNotBlank()) {
                        val dur = duration.toIntOrNull() ?: 25
                        onConfirm(subject, chapter, topic, type, dur, priority)
                    }
                }
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ReplanDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (newMinutes: Int) -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(currentMinutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dynamic Replanning") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "How much time do you actually have to study today?",
                    style = MaterialTheme.typography.bodyMedium
                )
                listOf(45, 60, 90, 120, 150, 180).forEach { mins ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedMinutes = mins }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMinutes == mins,
                            onClick = { selectedMinutes = mins }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${mins / 60}h ${mins % 60}m (${mins} minutes)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Text(
                    text = "Learnova will recalculate today's priority schedule without stress or guilt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedMinutes) }) {
                Text("Replan Day")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CustomizeWidgetsDialog(
    widgets: List<WidgetPreference>,
    onToggle: (WidgetPreference) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customize Dashboard Widgets") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                widgets.forEach { pref ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = pref.title, style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = pref.isVisible,
                            onCheckedChange = { onToggle(pref) }
                        )
                    }
                }
                TextButton(onClick = onReset, modifier = Modifier.align(Alignment.End)) {
                    Text("Reset to Defaults")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )
}
