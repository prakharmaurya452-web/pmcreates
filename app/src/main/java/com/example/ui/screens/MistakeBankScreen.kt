package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.Exam
import com.example.data.model.Mistake
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MistakeBankScreen(
    mistakes: List<Mistake>,
    exams: List<Exam>,
    onAddMistake: (
        question: String,
        studentAns: String,
        correctAns: String,
        explanation: String,
        subject: String,
        chapter: String,
        topic: String,
        mistakeType: String
    ) -> Unit,
    onUpdateMistakeStatus: (Mistake, newStatus: String) -> Unit,
    onDeleteMistake: (id: String) -> Unit,
    onStartFocusSession: (subject: String, topic: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var selectedSubjectFilter by remember { mutableStateOf("ALL") }
    var showAddMistakeDialog by remember { mutableStateOf(false) }

    // Pre-Exam Mistake Analysis
    val now = System.currentTimeMillis()
    val upcomingExam = exams.filter { it.examDateTimestamp > now }.minByOrNull { it.examDateTimestamp }
    val examDays = if (upcomingExam != null) ((upcomingExam.examDateTimestamp - now) / 86400000L).toInt() else 999
    val unresolvedExamMistakes = if (upcomingExam != null) {
        mistakes.filter {
            it.status == "Unresolved" && (
                it.subject.contains(upcomingExam.subjectNames, ignoreCase = true) ||
                upcomingExam.subjectNames.contains(it.subject, ignoreCase = true)
            )
        }
    } else emptyList()

    // Filtered mistakes
    val filteredMistakes = mistakes.filter { mistake ->
        val matchesStatus = when (selectedStatusFilter) {
            "ALL" -> true
            else -> mistake.status.equals(selectedStatusFilter, ignoreCase = true)
        }
        val matchesType = when (selectedTypeFilter) {
            "ALL" -> true
            else -> mistake.mistakeType.equals(selectedTypeFilter, ignoreCase = true)
        }
        val matchesSubject = when (selectedSubjectFilter) {
            "ALL" -> true
            else -> mistake.subject.equals(selectedSubjectFilter, ignoreCase = true)
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            mistake.question.contains(searchQuery, ignoreCase = true) ||
            mistake.studentAnswer.contains(searchQuery, ignoreCase = true) ||
            mistake.correctAnswer.contains(searchQuery, ignoreCase = true) ||
            mistake.explanation.contains(searchQuery, ignoreCase = true) ||
            mistake.topic.contains(searchQuery, ignoreCase = true) ||
            mistake.chapter.contains(searchQuery, ignoreCase = true)
        }
        matchesStatus && matchesType && matchesSubject && matchesSearch
    }

    // Counts
    val unresolvedCount = mistakes.count { it.status == "Unresolved" }
    val reviewingCount = mistakes.count { it.status == "Reviewing" }
    val resolvedCount = mistakes.count { it.status == "Resolved" }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddMistakeDialog = true },
                containerColor = BrandIndigo,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("log_mistake_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Mistake")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search question, concept, or formula...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("mistake_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
            ) {
                // Pre-Exam Recommendation Alert Banner
                if (upcomingExam != null && unresolvedExamMistakes.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandRose.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandRose.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pre_exam_mistake_recommendation")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = BrandRose)
                                    Text(
                                        text = "Pre-Exam Error Alert: ${upcomingExam.name}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = BrandRose
                                    )
                                }
                                Text(
                                    text = "You have ${unresolvedExamMistakes.size} unresolved mistake(s) in ${upcomingExam.subjectNames} with ${examDays} days left until your exam. Clearing error patterns now prevents repeat marks deduction in Section B/C.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            selectedSubjectFilter = upcomingExam.subjectNames.split(",").firstOrNull()?.trim() ?: upcomingExam.subjectNames
                                            selectedStatusFilter = "Unresolved"
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Review ${unresolvedExamMistakes.size} Unresolved Mistakes", style = MaterialTheme.typography.labelSmall)
                                    }

                                    unresolvedExamMistakes.firstOrNull()?.let { topMistake ->
                                        OutlinedButton(
                                            onClick = { onStartFocusSession(topMistake.subject, topMistake.topic) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Focus on ${topMistake.topic}", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Analytics Status Summary Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Mistake Bank Overview",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MistakeStatPill(
                                    label = "Unresolved",
                                    count = unresolvedCount,
                                    color = BrandRose,
                                    isSelected = selectedStatusFilter == "Unresolved",
                                    onClick = {
                                        selectedStatusFilter = if (selectedStatusFilter == "Unresolved") "ALL" else "Unresolved"
                                    }
                                )
                                MistakeStatPill(
                                    label = "Reviewing",
                                    count = reviewingCount,
                                    color = BrandAmber,
                                    isSelected = selectedStatusFilter == "Reviewing",
                                    onClick = {
                                        selectedStatusFilter = if (selectedStatusFilter == "Reviewing") "ALL" else "Reviewing"
                                    }
                                )
                                MistakeStatPill(
                                    label = "Resolved",
                                    count = resolvedCount,
                                    color = BrandEmerald,
                                    isSelected = selectedStatusFilter == "Resolved",
                                    onClick = {
                                        selectedStatusFilter = if (selectedStatusFilter == "Resolved") "ALL" else "Resolved"
                                    }
                                )
                            }
                        }
                    }
                }

                // Filter Category 1: Mistake Type Filter Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Categorized by Error Type:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val types = listOf(
                                "ALL" to "All Types",
                                "Concept" to "💡 Concept",
                                "Calculation" to "🧮 Calculation",
                                "Reading" to "📖 Reading",
                                "Memory" to "🧠 Memory",
                                "Careless" to "⚠️ Careless",
                                "Time Management" to "⏱️ Time Mgt"
                            )
                            items(types) { (typeKey, label) ->
                                FilterChip(
                                    selected = selectedTypeFilter == typeKey,
                                    onClick = { selectedTypeFilter = typeKey },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                // Filter Category 2: Subject Filter Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Filter by Subject:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val subjects = listOf("ALL", "Mathematics", "Science", "Social Science", "English")
                            items(subjects) { sub ->
                                val label = if (sub == "ALL") "All Subjects" else sub
                                FilterChip(
                                    selected = selectedSubjectFilter == sub,
                                    onClick = { selectedSubjectFilter = sub },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                // Mistakes Count and Reset Filters
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Showing ${filteredMistakes.size} of ${mistakes.size} Mistakes",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedStatusFilter != "ALL" || selectedTypeFilter != "ALL" || selectedSubjectFilter != "ALL" || searchQuery.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    selectedStatusFilter = "ALL"
                                    selectedTypeFilter = "ALL"
                                    selectedSubjectFilter = "ALL"
                                    searchQuery = ""
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Reset Filters", style = MaterialTheme.typography.labelSmall, color = BrandIndigo)
                            }
                        }
                    }
                }

                // Mistake Cards List
                if (filteredMistakes.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = BrandEmerald,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = if (selectedStatusFilter == "Unresolved") "No Unresolved Mistakes!" else "No mistakes match current filters",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (selectedStatusFilter == "Unresolved") "Great job! All your logged mistakes are resolved or currently being reviewed." else "Try adjusting your search query, type, or subject filter.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredMistakes) { mistake ->
                        MistakeDetailCard(
                            mistake = mistake,
                            onUpdateStatus = { newStatus -> onUpdateMistakeStatus(mistake, newStatus) },
                            onDelete = { onDeleteMistake(mistake.id) },
                            onStartFocus = { onStartFocusSession(mistake.subject, mistake.topic) }
                        )
                    }
                }
            }
        }
    }

    // Add Mistake Dialog
    if (showAddMistakeDialog) {
        AddMistakeFullDialog(
            onDismiss = { showAddMistakeDialog = false },
            onConfirm = { q, sAns, cAns, exp, sub, ch, top, type ->
                onAddMistake(q, sAns, cAns, exp, sub, ch, top, type)
                showAddMistakeDialog = false
            }
        )
    }
}

@Composable
private fun MistakeStatPill(
    label: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.25f) else color.copy(alpha = 0.1f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null,
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
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
private fun MistakeDetailCard(
    mistake: Mistake,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit,
    onStartFocus: () -> Unit
) {
    val statusColor = when (mistake.status) {
        "Resolved" -> BrandEmerald
        "Reviewing" -> BrandAmber
        else -> BrandRose
    }

    val typeColor = when (mistake.mistakeType) {
        "Concept" -> BrandViolet
        "Calculation" -> BrandCyan
        "Memory" -> BrandAmber
        "Reading" -> BrandIndigo
        "Careless" -> BrandRose
        else -> BrandIndigoDark
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = mistake.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Mistake Type Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${mistake.mistakeType} Error",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = typeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Subject Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = mistake.subject,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Topic Name
            Text(
                text = "${mistake.topic} • ${mistake.chapter}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = BrandIndigo
            )

            // Question / Problem Statement
            Text(
                text = mistake.question,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Student Answer Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BrandRose.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandRose.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "❌ Your Answer / Mistake:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BrandRose
                    )
                    Text(
                        text = mistake.studentAnswer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Correct Answer Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BrandEmerald.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "✓ Correct Method / Answer:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BrandEmerald
                    )
                    Text(
                        text = mistake.correctAnswer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Explanation / Key Insight
            if (mistake.explanation.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(18.dp))
                        Column {
                            Text(
                                text = "Concept Note & Prevention:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = mistake.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Status Actions & Focus Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Focus Button
                OutlinedButton(
                    onClick = onStartFocus,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Focus on Topic", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Transition Status buttons
                if (mistake.status != "Resolved") {
                    Button(
                        onClick = { onUpdateStatus("Resolved") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Mark Resolved", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (mistake.status == "Unresolved") {
                    OutlinedButton(
                        onClick = { onUpdateStatus("Reviewing") },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Reviewing", style = MaterialTheme.typography.labelSmall)
                    }
                } else if (mistake.status == "Resolved") {
                    OutlinedButton(
                        onClick = { onUpdateStatus("Unresolved") },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Reopen", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMistakeFullDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        question: String,
        studentAns: String,
        correctAns: String,
        exp: String,
        sub: String,
        ch: String,
        top: String,
        type: String
    ) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var studentAnswer by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Science") }
    var chapter by remember { mutableStateOf("Chapter 12: Electricity") }
    var topic by remember { mutableStateOf("Joule's Law of Heating") }
    var mistakeType by remember { mutableStateOf("Calculation") }

    val subjects = listOf("Science", "Mathematics", "Social Science", "English")
    val mistakeTypes = listOf("Concept", "Calculation", "Memory", "Reading", "Careless", "Time Management")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.ReportProblem, contentDescription = null, tint = BrandRose)
                Text("Log Academic Mistake")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Subject and Type Chips
                Text("Subject:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { s ->
                        FilterChip(
                            selected = subject == s,
                            onClick = { subject = s },
                            label = { Text(s, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text("Mistake Type:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(mistakeTypes) { mt ->
                        FilterChip(
                            selected = mistakeType == mt,
                            onClick = { mistakeType = mt },
                            label = { Text(mt, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                    label = { Text("Prevention Note / Key Concept") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.isNotBlank() && studentAnswer.isNotBlank()) {
                        onConfirm(question, studentAnswer, correctAnswer, explanation, subject, chapter, topic, mistakeType)
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
