package xyz.block.gosling

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSection(context: Context) {
    val sharedPrefs =
        context.getSharedPreferences(PreferenceKeys.PREF_FILE_NAME, Context.MODE_PRIVATE)

    val savedModel =
        sharedPrefs.getString(PreferenceKeys.SELECTED_MODEL, PreferenceKeys.DEFAULT_MODEL)
            ?: PreferenceKeys.DEFAULT_MODEL
    val savedApiKey = sharedPrefs.getString(PreferenceKeys.API_KEY, "") ?: ""

    var selectedModel by remember { mutableStateOf(savedModel) }
    var apiKey by remember { mutableStateOf(savedApiKey) }
    var showApiKey by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }

    val models = listOf("gpt-4o", "o3-mini", "claude", "gemini-flash")

    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            DropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                models.forEach { model ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                model,
                                color = if (model == selectedModel)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            selectedModel = model
                            expandedDropdown = false
                            sharedPrefs.edit().putString(PreferenceKeys.SELECTED_MODEL, model)
                                .apply()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
                sharedPrefs.edit().putString(PreferenceKeys.API_KEY, it).apply()
            },
            label = { Text("API Key") },
            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showApiKey = !showApiKey }) {
                    Text(
                        if (showApiKey) "Hide" else "Show",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

object PreferenceKeys {
    const val PREF_FILE_NAME = "gosling_settings"
    const val SELECTED_MODEL = "selected_model"
    const val API_KEY = "api_key"
    const val DEFAULT_MODEL = "gpt-4o"
}