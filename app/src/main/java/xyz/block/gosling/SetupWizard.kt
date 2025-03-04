package xyz.block.gosling

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import xyz.block.gosling.ui.theme.LocalGoslingColors
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

enum class SetupStep {
    WELCOME,
    ACCESSIBILITY_PERMISSION,
    ASSISTANT_SELECTION,
    LLM_CONFIG
}

@Composable
fun SetupWizard(
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier,
    settingsManager: SettingsManager,
    openAccessibilitySettings: () -> Unit,
    isAccessibilityEnabled: Boolean
) {
    var currentStep by remember { mutableStateOf(SetupStep.WELCOME) }
    var llmModel by remember { mutableStateOf(settingsManager.llmModel) }
    var apiKey by remember { mutableStateOf(settingsManager.apiKey) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = {
                when (currentStep) {
                    SetupStep.WELCOME -> 0.25f
                    SetupStep.ACCESSIBILITY_PERMISSION -> 0.5f
                    SetupStep.ASSISTANT_SELECTION -> 0.75f
                    SetupStep.LLM_CONFIG -> 1f
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Step content
        when (currentStep) {
            SetupStep.WELCOME -> {
                WelcomeStep(
                    onNext = { currentStep = SetupStep.ACCESSIBILITY_PERMISSION }
                )
            }
            SetupStep.ACCESSIBILITY_PERMISSION -> {
                AccessibilityPermissionStep(
                    isEnabled = isAccessibilityEnabled,
                    onEnable = openAccessibilitySettings,
                    onNext = { currentStep = SetupStep.ASSISTANT_SELECTION }
                )
            }
            SetupStep.ASSISTANT_SELECTION -> {
                AssistantSelectionStep(
                    onNext = { currentStep = SetupStep.LLM_CONFIG }
                )
            }
            SetupStep.LLM_CONFIG -> {
                LLMConfigStep(
                    llmModel = llmModel,
                    onLLMModelChange = { llmModel = it },
                    apiKey = apiKey,
                    onApiKeyChange = { apiKey = it },
                    onComplete = {
                        settingsManager.llmModel = llmModel
                        settingsManager.apiKey = apiKey
                        settingsManager.isFirstTime = false
                        onSetupComplete()
                    }
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Welcome to Gosling!",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Let's get you set up with your personal AI assistant.",
            style = MaterialTheme.typography.bodyLarge
        )
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }
    }
}

@Composable
private fun AccessibilityPermissionStep(
    isEnabled: Boolean,
    onEnable: () -> Unit,
    onNext: () -> Unit
) {
    val goslingColors = LocalGoslingColors.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Accessibility Permissions",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Gosling needs accessibility permissions to interact with other apps and help you with tasks.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            onClick = onEnable,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEnabled) goslingColors.secondaryButton else goslingColors.primaryBackground
            )
        ) {
            Text(if (isEnabled) "Accessibility Enabled" else "Enable Accessibility")
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = true
            ) {
                Text("Skip")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = isEnabled
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun AssistantSelectionStep(
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val goslingColors = LocalGoslingColors.current
    var isAssistantEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Check if our app is the current assistant
        val settingSecure = Settings.Secure.getString(
            context.contentResolver,
            "assistant"
        )
        isAssistantEnabled = settingSecure?.contains(context.packageName) == true
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Set Default Assistant",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Make Gosling your default assistant app to get the most out of its features.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAssistantEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAssistantEnabled) goslingColors.secondaryButton else goslingColors.primaryBackground
            )
        ) {
            Text(if (isAssistantEnabled) "Assistant Enabled" else "Set as Default Assistant")
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = true
            ) {
                Text("Skip")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = true
            ) {
                Text("Next")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LLMConfigStep(
    llmModel: String,
    onLLMModelChange: (String) -> Unit,
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    val models = listOf(
        "gpt-4o" to "GPT-4 Optimized",
        "o3-mini" to "O3 Mini",
        "o3-small" to "O3 Small",
        "o3-medium" to "O3 Medium",
        "o3-large" to "O3 Large"
    )
    
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "LLM Configuration",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Select your LLM model and enter your API key to get started.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        // Model Selection Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = models.find { it.first == llmModel }?.second ?: llmModel,
                onValueChange = {},
                readOnly = true,
                label = { Text("LLM Model") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                models.forEach { (modelId, displayName) ->
                    DropdownMenuItem(
                        text = { Text(displayName) },
                        onClick = {
                            onLLMModelChange(modelId)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChange,
            label = { Text("API Key") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(),
            enabled = llmModel.isNotEmpty() && apiKey.isNotEmpty()
        ) {
            Text("Complete Setup")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    var llmModel by remember { mutableStateOf(settingsManager.llmModel) }
    var apiKey by remember { mutableStateOf(settingsManager.apiKey) }
    var showResetDialog by remember { mutableStateOf(false) }

    val models = listOf(
        "gpt-4o" to "GPT-4 Optimized",
        "o3-mini" to "O3 Mini",
        "o3-small" to "O3 Small",
        "o3-medium" to "O3 Medium",
        "o3-large" to "O3 Large"
    )
    
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge
            )
            // Empty box for alignment
            Box(modifier = Modifier.width(48.dp))
        }

        // Model Selection Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = models.find { it.first == llmModel }?.second ?: llmModel,
                onValueChange = {},
                readOnly = true,
                label = { Text("LLM Model") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                models.forEach { (modelId, displayName) ->
                    DropdownMenuItem(
                        text = { Text(displayName) },
                        onClick = {
                            llmModel = modelId
                            settingsManager.llmModel = modelId
                            expanded = false
                        }
                    )
                }
            }
        }
        
        OutlinedTextField(
            value = apiKey,
            onValueChange = { 
                apiKey = it
                settingsManager.apiKey = it
            },
            label = { Text("API Key") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { showResetDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Reset Setup")
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Setup") },
            text = { Text("This will reset all settings and show the setup wizard again. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsManager.isFirstTime = true
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 
