package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material.icons.outlined.Vrpano
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.UnityProjectEntity
import com.example.ui.theme.LtsEmerald
import com.example.ui.theme.LtsEmeraldContainer
import com.example.ui.theme.UnityBlue
import com.example.ui.theme.UnityBlueContainer
import com.example.ui.theme.UnityIndigo
import com.example.ui.theme.UnityIndigoContainer
import com.example.ui.theme.WarnAmber
import com.example.ui.theme.WarnAmberContainer

private val projectFilters = listOf("All", "Favorites", "3D URP", "2D", "VR / XR")

@Composable
fun ProjectsScreen(
    projects: List<UnityProjectEntity>,
    allProjectCount: Int,
    searchQuery: String,
    selectedFilter: String,
    bannerMessage: String?,
    onSearchQueryChange: (String) -> Unit,
    onSelectFilter: (String) -> Unit,
    onProjectClick: (UnityProjectEntity) -> Unit,
    onToggleFavorite: (UnityProjectEntity) -> Unit,
    onLaunchProject: (UnityProjectEntity) -> Unit,
    onCreateProjectClick: () -> Unit,
    onDismissBanner: () -> Unit,
    onOpenBrandingSite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize()
                .testTag("projects_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ProjectsOverviewHeader(
                    totalProjects = allProjectCount,
                    onCreateProjectClick = onCreateProjectClick,
                    onOpenBrandingSite = onOpenBrandingSite
                )
            }

            item {
                AnimatedVisibility(visible = bannerMessage != null) {
                    if (bannerMessage != null) {
                        Surface(
                            color = LtsEmeraldContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("project_status_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = LtsEmerald
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = bannerMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = onDismissBanner,
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Dismiss notification",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(stringResource(R.string.search_projects_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("projects_search_input")
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    projectFilters.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { onSelectFilter(filter) },
                            label = { Text(filter) },
                            modifier = Modifier.testTag("project_filter_$filter")
                        )
                    }
                }
            }

            if (projects.isEmpty()) {
                item {
                    EmptyProjectsCard(
                        hasSearchOrFilter = searchQuery.isNotBlank() || selectedFilter != "All",
                        onClearFilters = {
                            onSearchQueryChange("")
                            onSelectFilter("All")
                        },
                        onCreateProjectClick = onCreateProjectClick
                    )
                }
            } else {
                items(projects, key = { it.id }) { project ->
                    UnityProjectCard(
                        project = project,
                        onClick = { onProjectClick(project) },
                        onToggleFavorite = { onToggleFavorite(project) },
                        onLaunch = { onLaunchProject(project) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun ProjectsOverviewHeader(
    totalProjects: Int,
    onCreateProjectClick: () -> Unit,
    onOpenBrandingSite: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Projects Workspace",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$totalProjects local projects • Unity 6 LTS Ready",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = onCreateProjectClick,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("header_new_project_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New")
                    }
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = LtsEmeraldContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Editor: 6000.0.28f1 LTS",
                            style = MaterialTheme.typography.labelMedium,
                            color = LtsEmerald,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    AssistChip(
                        onClick = onOpenBrandingSite,
                        label = { Text("Branding & Trademarks") },
                        modifier = Modifier.testTag("projects_header_branding_chip")
                    )
                }
            }
        }
    }
}

@Composable
private fun UnityProjectCard(
    project: UnityProjectEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onLaunch: () -> Unit
) {
    val badgeStyle = templateBadgeStyle(project.template)

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("project_card_${project.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeStyle.containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = badgeStyle.icon,
                        contentDescription = null,
                        tint = badgeStyle.iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = project.localPath,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("project_favorite_${project.id}")
                ) {
                    Icon(
                        imageVector = if (project.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = stringResource(R.string.cd_favorite),
                        tint = if (project.isFavorite) WarnAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (project.description.isNotBlank()) {
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MetaTagPill(text = project.template)
                    MetaTagPill(text = project.editorVersion.substringBefore(" "))
                    MetaTagPill(text = project.targetPlatform)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onLaunch,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("launch_project_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun MetaTagPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyProjectsCard(
    hasSearchOrFilter: Boolean,
    onClearFilters: () -> Unit,
    onCreateProjectClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = if (hasSearchOrFilter) "No matching Unity projects" else "Your Unity Hub is empty",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (hasSearchOrFilter) {
                    "Try clearing the active filter or search term."
                } else {
                    "Create your first Unity project to get started."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = if (hasSearchOrFilter) onClearFilters else onCreateProjectClick,
                modifier = Modifier.height(48.dp)
            ) {
                Text(if (hasSearchOrFilter) "Reset Filters" else "Create Project")
            }
        }
    }
}

private data class TemplateBadgeStyle(
    val icon: ImageVector,
    val iconColor: Color,
    val containerColor: Color
)

private fun templateBadgeStyle(template: String): TemplateBadgeStyle {
    return when {
        template.contains("2D", ignoreCase = true) -> TemplateBadgeStyle(
            icon = Icons.Outlined.Layers,
            iconColor = WarnAmber,
            containerColor = WarnAmberContainer
        )
        template.contains("VR", ignoreCase = true) || template.contains("XR", ignoreCase = true) -> TemplateBadgeStyle(
            icon = Icons.Outlined.Vrpano,
            iconColor = UnityIndigo,
            containerColor = UnityIndigoContainer
        )
        template.contains("Mobile", ignoreCase = true) -> TemplateBadgeStyle(
            icon = Icons.Outlined.PhoneAndroid,
            iconColor = LtsEmerald,
            containerColor = LtsEmeraldContainer
        )
        template.contains("3D", ignoreCase = true) -> TemplateBadgeStyle(
            icon = Icons.Outlined.ViewInAr,
            iconColor = UnityBlue,
            containerColor = UnityBlueContainer
        )
        else -> TemplateBadgeStyle(
            icon = Icons.Outlined.Devices,
            iconColor = UnityBlue,
            containerColor = UnityBlueContainer
        )
    }
}
