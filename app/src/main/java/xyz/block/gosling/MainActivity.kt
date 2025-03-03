package xyz.block.gosling

import GoslingUI
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import xyz.block.gosling.ui.theme.GoslingTheme
import xyz.block.gosling.ui.theme.LocalGoslingColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoslingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        GoslingApplication.isMainActivityRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        GoslingApplication.isMainActivityRunning = false
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isDefaultAssistant = remember { checkDefaultAssistant(context) }
    val isAccessibilityEnabled = remember { checkAccessibilityPermission(context) }

    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Default Assistant: ${if (isDefaultAssistant) "Yes" else "No"}",
                modifier = Modifier.clickable { openAssistantSettings(context) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Accessibility Enabled: ${if (isAccessibilityEnabled) "Yes" else "No"}",
                modifier = Modifier.clickable { openAccessibilitySettings(context) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showSettings = !showSettings },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalGoslingColors.current.secondaryButton,
                    contentColor = LocalGoslingColors.current.primaryText
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = if (showSettings) "Hide Settings" else "Show Settings")
            }

            if (showSettings) {
                SettingsSection(context = context)
            }
        }

        GoslingUI(
            modifier = Modifier.padding(bottom = 0.dp),
            context = context
        )
    }
}


fun checkDefaultAssistant(context: Context): Boolean {
    val assistant = Settings.Secure.getString(context.contentResolver, "assistant")
    return assistant?.contains(context.packageName) == true
}

fun checkAccessibilityPermission(context: Context): Boolean {
    val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    return enabledServices?.contains(context.packageName) == true
}

fun openAssistantSettings(context: Context) {
    val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
    context.startActivity(intent)
}

fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    context.startActivity(intent)
}
