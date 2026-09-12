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
import com.example.data.model.Chapter
import com.example.data.model.Subject
import com.example.data.model.Topic
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen(
    subjects: List<Subject>,
    chapters: List<Chapter>,
    topics: List<Topic>,
    onAddSubject: (name: String, color: String, targetHours: Int) -> Unit,
    onDeleteSubject: (id: String) -> Unit,
    onAddChapter: (subjectId: String, title: String) -> Unit,
    onDeleteChapter: (id: String) -> Unit,
    onAddTopic: (chapterId: String, subjectId: String, title: String, estimatedMinutes: Int) -> Unit,
    onUpdateTopic: (Topic, newStatus: String, newAccuracy: Int, newConfidence: Int) -> Unit,
    onDeleteTopic: (id: String) -> Unit,
    onStartFocusSession: (subject: String, topic: String) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var activeChapterForAddTopic by remember { mutableStateOf<Chapter?>(null) }
    var selectedTopicForEdit by remember { mutableStateOf<Topic?>(null) }

    // Initialize selected subject if not set
    LaunchedEffect(subjects) {
        if (selectedSubjectId == null && subjects.isNotEmpty()) {
            selectedSubjectId = subjects.first().id
        }
    }

    val currentSubject = subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()
    val subjectChapters = chapters.filter { it.subjectId == currentSubject?.id }

    // Syllabus coverage metrics
    val subjectTopics = topics.filter { it.subjectId == currentSubject?.id }
    val totalTopicsCount = subjectTopics.size
    val masteredTopicsCount = subjectTopics.count { it.status == "Mastered" }
    val revisedTopicsCount = subjectTopics.count { it.status == "Revised" || it.status == "Mastered" }
    val subjectProgress = if (totalTopicsCount > 0) (revisedTopicsCount.toFloat() / totalTopicsCount.toFloat()) else 0f

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSubjectDialog = true },
                containerColor = BrandIndigo,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_custom_subject_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Subject")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Subject Scrollable Tabs
            ScrollableTabRow(
                selectedTabIndex = subjects.indexOfFirst { it.id == selectedSubjectId }.coerceAtLeast(0),
                edgePadding = 16.dp,
                divider = {},
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                subjects.forEach { subject ->
                    val isSelected = subject.id == selectedSubjectId
                    Tab(
                        selected = isSelected,
                        onClick = { selectedSubjectId = subject.id },
                        text = {
                            Text(
                                text = subject.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Subject Progress Overview Card
            if (currentSubject != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
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
                            Column {
                                Text(
                                    text = "${currentSubject.name} Syllabus",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "$masteredTopicsCount Mastered • $revisedTopicsCount Revised of $totalTopicsCount Topics",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${(subjectProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = BrandIndigo
                            )
                        }
                        LinearProgressIndicator(
                            progress = subjectProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = BrandIndigo,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Search and Status Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search topics...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedButton(
                    onClick = { showAddChapterDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chapter")
                }
            }

            // Chapter & Topic List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
            ) {
                items(subjectChapters) { chapter ->
                    val chapterTopics = topics.filter { it.chapterId == chapter.id }
                        .filter {
                            if (searchQuery.isBlank()) true
                            else it.title.contains(searchQuery, ignoreCase = true)
                        }

                    ChapterCard(
                        chapter = chapter,
                        topics = chapterTopics,
                        subjectName = currentSubject?.name ?: "",
                        onAddTopicClick = {
                            activeChapterForAddTopic = chapter
                            showAddTopicDialog = true
                        },
                        onDeleteChapter = { onDeleteChapter(chapter.id) },
                        onTopicClick = { topic -> selectedTopicForEdit = topic },
                        onStartFocus = { topic ->
                            onStartFocusSession(currentSubject?.name ?: "Subject", topic.title)
                        }
                    )
                }
            }
        }
    }

    // Add Subject Dialog
    if (showAddSubjectDialog) {
        AddSubjectDialog(
            onDismiss = { showAddSubjectDialog = false },
            onConfirm = { name, color, hours ->
                onAddSubject(name, color, hours)
                showAddSubjectDialog = false
            }
        )
    }

    // Add Chapter Dialog
    if (showAddChapterDialog && currentSubject != null) {
        AddChapterDialog(
            subjectName = currentSubject.name,
            onDismiss = { showAddChapterDialog = false },
            onConfirm = { title ->
                onAddChapter(currentSubject.id, title)
                showAddChapterDialog = false
            }
        )
    }

    // Add Topic Dialog
    if (showAddTopicDialog && activeChapterForAddTopic != null && currentSubject != null) {
        AddTopicDialog(
            chapterTitle = activeChapterForAddTopic!!.title,
            onDismiss = { showAddTopicDialog = false },
            onConfirm = { title, minutes ->
                onAddTopic(activeChapterForAddTopic!!.id, currentSubject.id, title, minutes)
                showAddTopicDialog = false
            }
        )
    }

    // Edit Topic Status Dialog
    if (selectedTopicForEdit != null) {
        EditTopicDialog(
            topic = selectedTopicForEdit!!,
            onDismiss = { selectedTopicForEdit = null },
            onUpdate = { newStatus, accuracy, confidence ->
                onUpdateTopic(selectedTopicForEdit!!, newStatus, accuracy, confidence)
                selectedTopicForEdit = null
            },
            onDelete = {
                onDeleteTopic(selectedTopicForEdit!!.id)
                selectedTopicForEdit = null
            }
        )
    }
}

@Composable
private fun ChapterCard(
    chapter: Chapter,
    topics: List<Topic>,
    subjectName: String,
    onAddTopicClick: () -> Unit,
    onDeleteChapter: () -> Unit,
    onTopicClick: (Topic) -> Unit,
    onStartFocus: (Topic) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAddTopicClick) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Topic", tint = BrandIndigo)
                    }
                }
            }

            if (topics.isEmpty()) {
                Text(
                    text = "No topics added yet. Tap + to add topics to this chapter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topics.forEach { topic ->
                        TopicItemRow(
                            topic = topic,
                            onClick = { onTopicClick(topic) },
                            onStartFocus = { onStartFocus(topic) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicItemRow(
    topic: Topic,
    onClick: () -> Unit,
    onStartFocus: () -> Unit
) {
    val statusColor = when (topic.status) {
        "Mastered" -> BrandEmerald
        "Revised" -> BrandIndigoLight
        "Practicing" -> BrandCyan
        "Learning" -> BrandAmber
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() }
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = topic.status,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "Acc: ${topic.accuracyPercent}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "⭐ ${topic.confidenceLevel}/5",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onStartFocus) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Focus", tint = BrandIndigo)
        }
    }
}

@Composable
private fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: String, targetHours: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("6") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Subject") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name (e.g. Computer Science)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Target Hours Per Week") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, "#4F46E5", hours.toIntOrNull() ?: 6)
                    }
                }
            ) {
                Text("Add Subject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddChapterDialog(
    subjectName: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Chapter to $subjectName") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Chapter Title") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(title) }) {
                Text("Add Chapter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddTopicDialog(
    chapterTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, minutes: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("25") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Topic to $chapterTitle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Topic Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it },
                    label = { Text("Estimated Study Minutes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(title, minutes.toIntOrNull() ?: 25) }) {
                Text("Add Topic")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditTopicDialog(
    topic: Topic,
    onDismiss: () -> Unit,
    onUpdate: (newStatus: String, accuracy: Int, confidence: Int) -> Unit,
    onDelete: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(topic.status) }
    var accuracy by remember { mutableStateOf(topic.accuracyPercent.toString()) }
    var confidence by remember { mutableStateOf(topic.confidenceLevel) }

    val statusOptions = listOf("Not Started", "Learning", "Practicing", "Revised", "Mastered")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(topic.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Status", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    statusOptions.forEach { st ->
                        FilterChip(
                            selected = selectedStatus == st,
                            onClick = { selectedStatus = st },
                            label = { Text(st, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = accuracy,
                    onValueChange = { accuracy = it },
                    label = { Text("Accuracy (%)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Confidence (1-5 Stars)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { confidence = star }) {
                            Icon(
                                imageVector = if (star <= confidence) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (star <= confidence) BrandAmber else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val acc = accuracy.toIntOrNull()?.coerceIn(0, 100) ?: topic.accuracyPercent
                    onUpdate(selectedStatus, acc, confidence)
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text("Delete Topic", color = BrandRose)
            }
        }
    )
}
