package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicResource
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    resources: List<AcademicResource>,
    onToggleSaved: (AcademicResource) -> Unit,
    onUpdateProgress: (AcademicResource, newStatus: String) -> Unit,
    onAddToPlan: (AcademicResource) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedSubject by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf(
        "ALL" to "All Resources",
        "OFFICIAL_CBSE" to "🏛️ Official CBSE",
        "PYQ" to "📄 PYQs",
        "SAMPLE_PAPER" to "📝 Sample Papers",
        "COMPETENCY" to "🎯 Competency Bank",
        "UDAAN_LECTURE" to "🎥 PW Udaan",
        "CBSE_UPDATE" to "📢 Circulars",
        "SAVED" to "⭐ Saved"
    )

    val filteredResources = resources.filter { res ->
        val matchesCategory = when (selectedCategory) {
            "ALL" -> true
            "SAVED" -> res.isSaved
            else -> res.resourceType == selectedCategory
        }
        val matchesSubject = when (selectedSubject) {
            "ALL" -> true
            else -> res.subject == selectedSubject || res.subject == "All"
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            res.title.contains(searchQuery, ignoreCase = true) ||
                    res.description.contains(searchQuery, ignoreCase = true) ||
                    res.tags.contains(searchQuery, ignoreCase = true)
        }
        matchesCategory && matchesSubject && matchesSearch
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search CBSE sample papers, PYQs, lectures...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Scrollable Filter Chips
            ScrollableTabRow(
                selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0),
                edgePadding = 16.dp,
                divider = {},
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                categories.forEach { (catKey, label) ->
                    val isSelected = selectedCategory == catKey
                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategory = catKey },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Subject Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "Mathematics", "Science", "All").forEach { sub ->
                    val label = if (sub == "ALL") "All Subjects" else sub
                    FilterChip(
                        selected = selectedSubject == sub,
                        onClick = { selectedSubject = sub },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Resource List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                if (filteredResources.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No academic resources match the selected filters.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredResources) { resource ->
                        ResourceCard(
                            resource = resource,
                            onOpen = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.sourceUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle missing browser
                                }
                            },
                            onToggleSaved = { onToggleSaved(resource) },
                            onAddToPlan = { onAddToPlan(resource) },
                            onUpdateProgress = { newStatus -> onUpdateProgress(resource, newStatus) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceCard(
    resource: AcademicResource,
    onOpen: () -> Unit,
    onToggleSaved: () -> Unit,
    onAddToPlan: () -> Unit,
    onUpdateProgress: (String) -> Unit
) {
    val trustBadgeColor = when (resource.trustLevel) {
        "OFFICIAL_CBSE" -> BrandEmerald
        "VERIFIED_EDUCATIONAL" -> BrandIndigo
        else -> BrandCyan
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
            // Header Badges
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
                        shape = RoundedCornerShape(6.dp),
                        color = trustBadgeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (resource.trustLevel == "OFFICIAL_CBSE") "✓ OFFICIAL CBSE" else "VERIFIED EDUCATION",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = trustBadgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = resource.subject,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onToggleSaved,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (resource.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Resource",
                        tint = if (resource.isSaved) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Title & Description
            Text(
                text = resource.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = resource.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Metadata info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: ${resource.sourceName} • ${resource.academicSession}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (resource.duration.isNotBlank()) {
                    Text(
                        text = "⏱️ ${resource.duration}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = BrandViolet
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpen,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.2f).testTag("open_resource_button")
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Resource", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onAddToPlan,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("add_resource_to_plan_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to Plan", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
