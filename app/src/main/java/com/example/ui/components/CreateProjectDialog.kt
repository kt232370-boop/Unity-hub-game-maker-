package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

private val projectTemplates = listOf("3D (URP)", "2D (Built-in)", "VR Core", "Mobile 3D", "HDRP")
private val editorVersions = listOf("6000.0.28f1 LTS", "2022.3.51f1 LTS", "6000.1.0b2")
private val targetPlatforms = listOf("Android", "Windows / Mac", "Meta Quest XR", "WebGL")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, template: String, editorVersion: String, targetPlatform: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(projectTemplates.first()) }
    var selectedVersion by remember { mutableStateOf(editorVersions.first()) }
    var selectedPlatform by remember { mutableStateOf(targetPlatforms.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Unity Project",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project name") },
                    placeholder = { Text("e.g. CyberRunner3D") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_project_name_input")
                )

                Text(
                    text = "Template",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    projectTemplates.forEach { template ->
                        FilterChip(
                            selected = selectedTemplate == template,
                            onClick = { selectedTemplate = template },
                            label = { Text(template) },
                            modifier = Modifier.testTag("template_chip_$template")
                        )
                    }
                }

                Text(
                    text = "Unity Editor Version",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    editorVersions.forEach { version ->
                        FilterChip(
                            selected = selectedVersion == version,
                            onClick = { selectedVersion = version },
                            label = { Text(version) }
                        )
                    }
                }

                Text(
                    text = "Target Platform",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    targetPlatforms.forEach { platform ->
                        FilterChip(
                            selected = selectedPlatform == platform,
                            onClick = { selectedPlatform = platform },
                            label = { Text(platform) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("Render pipeline or scene notes") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_project_description_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(
                        name,
                        selectedTemplate,
                        selectedVersion,
                        selectedPlatform,
                        description
                    )
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_project_button")
            ) {
                Text("Create Project")
            }
        },
        dismissButton = {
            Row(modifier = Modifier.padding(end = 4.dp)) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_create_project_button")
                ) {
                    Text("Cancel")
                }
            }
        }
    )
    Spacer(modifier = Modifier.height(0.dp))
}
