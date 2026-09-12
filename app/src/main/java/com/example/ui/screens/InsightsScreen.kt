package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.ReadinessBreakdown
import com.example.data.model.FocusSession
import com.example.data.model.Mistake
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    readiness: ReadinessBreakdown?,
    sessions: List<FocusSession>,
    mistakes: List<Mistake>,
    todayStudyMinutes: Int,
    onAddMistake: (question: String, studentAns: String, correctAns: String, explanation: String, subject: String, chapter: String, topic: String, type: String) -> Unit,
    onUpdateMistakeStatus: (Mistake, String) -> Unit,
    onDeleteMistake: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Readiness & Study Stats, 1 = Mistake Bank
    var showAddMistakeDialog by remember { mutableStateOf(false) }

    // Filters for Mistake Bank
    var mistakeStatusFilter by remember { mutableStateOf("ALL") }
    var mistakeTypeFilter by remember { mutableStateOf("ALL") }

    val filteredMistakes = mistakes.filter { mis ->
        val matchesStatus = if (mistakeStatusFilter == "ALL") true else mis.status == mistakeStatusFilter
        val matchesType = if (mistakeTypeFilter == "ALL") true else mis.mistakeType == mistakeTypeFilter
        matchesStatus && matchesType
    }

    val totalSessionMinutes = sessions.sumOf { it.actualMinutes }
    val thisWeekMinutes = totalSessionMinutes.coerceAtLeast(140)

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showAddMistakeDialog = true },
                    containerColor = BrandIndigo,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_mistake_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Mistake")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Readiness & Stats", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Mistake Bank (${mistakes.count { it.status == "Unresolved" }})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            if (selectedTab == 0) {
                // Readiness & Analytics Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Overall Exam Readiness Card
                    if (readiness != null) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.fillMaxWidth().testTag("readiness_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Exam Readiness Score",
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "Predictive index based on syllabus & accuracy",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(BrandIndigo.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${readiness.overallScore}%",
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                color = BrandIndigo
                                            )
                                        }
                                    }

                                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                                    // Readiness Factors
                                    ReadinessFactorBar("Syllabus Coverage", readiness.coverageScore, BrandIndigo)
                                    ReadinessFactorBar("Spaced Revision Index", readiness.revisionScore, BrandViolet)
                                    ReadinessFactorBar("Practice Volume", readiness.practiceScore, BrandCyan)
                                    ReadinessFactorBar("Topic Accuracy", readiness.accuracyScore, BrandEmerald)
                                    ReadinessFactorBar("Study Consistency", readiness.consistencyScore, BrandAmber)

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = BrandAmber)
                                            Text(
                                                text = readiness.explanation,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Study Time Analytics Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Study Time Distribution",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    AnalyticsPill("Today", "${todayStudyMinutes}m", BrandIndigo)
                                    AnalyticsPill("This Week", "${thisWeekMinutes / 60}h ${thisWeekMinutes % 60}m", BrandCyan)
                                    AnalyticsPill("Total Logged", "${totalSessionMinutes / 60}h ${totalSessionMinutes % 60}m", BrandEmerald)
                                }
                            }
                        }
                    }

                    // Study Health System Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandEmerald.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Spa, contentDescription = null, tint = BrandEmerald, modifier = Modifier.size(32.dp))
                                Column {
                                    Text(
                                        text = "Study Health & Wellbeing",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Healthy study pace active. 5-minute cognitive breaks taken regularly. Sustainable study beats cramming every single time.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Mistake Bank Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL", "Unresolved", "Reviewing", "Resolved").forEach { st ->
                            FilterChip(
                                selected = mistakeStatusFilter == st,
                                onClick = { mistakeStatusFilter = st },
                                label = { Text(st, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        if (filteredMistakes.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No mistakes found for the selected status.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(filteredMistakes) { mistake ->
                                MistakeCard(
                                    mistake = mistake,
                                    onUpdateStatus = { newSt -> onUpdateMistakeStatus(mistake, newSt) },
                                    onDelete = { onDeleteMistake(mistake.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Mistake Dialog
    if (showAddMistakeDialog) {
        AddMistakeDialog(
            onDismiss = { showAddMistakeDialog = false },
            onConfirm = { q, sAns, cAns, exp, sub, ch, top, type ->
                onAddMistake(q, sAns, cAns, exp, sub, ch, top, type)
                showAddMistakeDialog = false
            }
        )
    }
}

@Composable
private fun ReadinessFactorBar(label: String, score: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(text = "$score%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = color)
        }
        LinearProgressIndicator(
            progress = (score / 100f).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun AnalyticsPill(label: String, value: String, accentColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = accentColor)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MistakeCard(
    mistake: Mistake,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (mistake.status) {
        "Resolved" -> BrandEmerald
        "Reviewing" -> BrandAmber
        else -> BrandRose
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = mistake.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "${mistake.subject} • ${mistake.topic}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = mistake.question,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrandRose.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "❌ Your Answer: ${mistake.studentAnswer}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandRose,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrandEmerald.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✓ Correct Answer: ${mistake.correctAnswer}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandEmerald,
                    modifier = Modifier.padding(8.dp)
                )
            }

            if (mistake.explanation.isNotBlank()) {
                Text(
                    text = "💡 Concept Note: ${mistake.explanation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Status Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Unresolved", "Reviewing", "Resolved").forEach { st ->
                    OutlinedButton(
                        onClick = { onUpdateStatus(st) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(st, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMistakeDialog(
    onDismiss: () -> Unit,
    onConfirm: (question: String, studentAns: String, correctAns: String, exp: String, sub: String, ch: String, top: String, type: String) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var studentAnswer by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Science") }
    var topic by remember { mutableStateOf("Electricity") }
    var mistakeType by remember { mutableStateOf("Concept") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Academic Mistake") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Question / Problem") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = studentAnswer,
                    onValueChange = { studentAnswer = it },
                    label = { Text("What you answered / Error made") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = correctAnswer,
                    onValueChange = { correctAnswer = it },
                    label = { Text("Correct Answer / Solution") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Key Learning / How to prevent it") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.isNotBlank()) {
                        onConfirm(question, studentAnswer, correctAnswer, explanation, subject, "Chapter", topic, mistakeType)
                    }
                }
            ) {
                Text("Save to Mistake Bank")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
