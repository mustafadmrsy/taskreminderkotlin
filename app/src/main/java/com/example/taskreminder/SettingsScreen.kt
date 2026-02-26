package com.example.taskreminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    state: SettingsState,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onNavigateToBlockedApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Ayarlar",
            style = MaterialTheme.typography.headlineSmall,
        )

        FilledTonalButton(
            onClick = onNavigateToBlockedApps,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Yasaklı uygulamalar")
        }

        Text(
            text = "Tema",
            style = MaterialTheme.typography.titleMedium,
        )

        ThemeModeOptionRow(
            label = "Sistem",
            selected = state.themeMode == ThemeMode.System,
            onClick = { onThemeModeChanged(ThemeMode.System) },
        )
        ThemeModeOptionRow(
            label = "Açık",
            selected = state.themeMode == ThemeMode.Light,
            onClick = { onThemeModeChanged(ThemeMode.Light) },
        )
        ThemeModeOptionRow(
            label = "Koyu",
            selected = state.themeMode == ThemeMode.Dark,
            onClick = { onThemeModeChanged(ThemeMode.Dark) },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dynamic Color",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.padding(top = 2.dp))
                Text(
                    text = "Android 12+ cihazlarda sistem renklerini kullanır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Switch(
                checked = state.dynamicColorEnabled,
                onCheckedChange = onDynamicColorChanged,
            )
        }
    }
}

@Composable
private fun ThemeModeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
