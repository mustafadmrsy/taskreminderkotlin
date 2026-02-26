package com.example.taskreminder

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch

@Composable
fun BlockedAppsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val blockedPackages by FocusPreferences.blockedPackagesFlow(context)
        .collectAsStateWithLifecycle(initialValue = emptySet())

    val focusEnabled by FocusPreferences.focusEnabledFlow(context)
        .collectAsStateWithLifecycle(initialValue = false)
    val focusEnd by FocusPreferences.focusEndEpochMillisFlow(context)
        .collectAsStateWithLifecycle(initialValue = 0L)

    val installedApps = remember {
        context.packageManager.getInstalledApplications(0)
            .asSequence()
            .filter { appInfo ->
                context.packageManager.getLaunchIntentForPackage(appInfo.packageName) != null
            }
            .sortedBy { appInfo ->
                context.packageManager.getApplicationLabel(appInfo).toString().lowercase()
            }
            .toList()
    }

    val quickPackages = remember {
        listOf(
            "com.instagram.android" to "Instagram",
            "com.google.android.youtube" to "YouTube",
            "com.zhiliaoapp.musically" to "TikTok",
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Yasaklı Uygulamalar",
            style = MaterialTheme.typography.headlineSmall,
        )

        Text(
            text = "Odak modu aktifken bu uygulamalar açılınca engel ekranı gösterilir.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = onNavigateBack, modifier = Modifier.weight(1f)) {
                Text("Geri")
            }

            Button(
                onClick = { openFocusAccessibilityServiceSettings(context) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Accessibility")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilledTonalButton(
                onClick = { openAppInfoSettings(context) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Uygulama ayarları")
            }

            FilledTonalButton(
                onClick = {
                    scope.launch {
                        FocusPreferences.startFocusSession(context.applicationContext, 5 * 60_000L)
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Odak başlat (5dk)")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilledTonalButton(
                onClick = {
                    scope.launch {
                        FocusPreferences.stopFocusSession(context.applicationContext)
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Odağı durdur")
            }

            FilledTonalButton(
                onClick = {
                    context.startActivity(
                        Intent(context, BlockScreenActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra(BlockScreenActivity.ExtraBlockedPackage, "TEST")
                    )
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Blok ekranı test")
            }
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        var accessibilityEnabled by remember { mutableStateOf(isFocusAccessibilityEnabled(context)) }
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    accessibilityEnabled = isFocusAccessibilityEnabled(context)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        val now = System.currentTimeMillis()
        val remainingMillis = (focusEnd - now).coerceAtLeast(0L)
        val remainingSeconds = remainingMillis / 1000L
        Text(
            text = "Durum: Accessibility=${if (accessibilityEnabled) "Açık" else "Kapalı"} | " +
                "Odak=${if (FocusPreferences.isFocusActive(focusEnabled, focusEnd)) "Aktif" else "Pasif"} | " +
                "Yasaklı=${blockedPackages.size}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = "Odak ham değerleri: enabled=$focusEnabled end=$focusEnd kalan=${remainingSeconds}s",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = "Hızlı ekle",
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            quickPackages.forEach { (pkg, label) ->
                FilledTonalButton(
                    onClick = {
                        val isInstalled = context.packageManager.getLaunchIntentForPackage(pkg) != null
                        if (!isInstalled) return@FilledTonalButton

                        scope.launch {
                            FocusPreferences.setBlockedPackages(context, blockedPackages + pkg)
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = installedApps, key = { it.packageName }) { appInfo ->
                BlockedAppRow(
                    context = context,
                    appInfo = appInfo,
                    checked = blockedPackages.contains(appInfo.packageName),
                    onCheckedChange = { checked ->
                        val updated = if (checked) {
                            blockedPackages + appInfo.packageName
                        } else {
                            blockedPackages - appInfo.packageName
                        }
                        scope.launch {
                            FocusPreferences.setBlockedPackages(context, updated)
                        }
                    },
                )
            }
        }
    }
}

private fun openFocusAccessibilityServiceSettings(context: Context) {
    val component = ComponentName(context, FocusAccessibilityService::class.java).flattenToString()
    val detailsIntent = Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS")
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra("android.provider.extra.ACCESSIBILITY_SERVICE", component)

    fun tryStart(intent: Intent): Boolean {
        return try {
            val resolved = intent.resolveActivity(context.packageManager) != null
            if (!resolved) return false
            context.startActivity(intent)
            true
        } catch (_: Throwable) {
            false
        }
    }

    val fallbackIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    if (tryStart(detailsIntent)) return
    tryStart(fallbackIntent)
}

private fun openAppInfoSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setData(Uri.parse("package:${context.packageName}"))
    try {
        context.startActivity(intent)
    } catch (_: Throwable) {
    }
}

private fun isFocusAccessibilityEnabled(context: Context): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false

    val expected = "${context.packageName}/${FocusAccessibilityService::class.java.name}"
    return enabledServices.split(':').any { it.equals(expected, ignoreCase = true) }
}

@Composable
private fun BlockedAppRow(
    context: Context,
    appInfo: ApplicationInfo,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val label = remember(appInfo.packageName) {
        context.packageManager.getApplicationLabel(appInfo).toString()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.padding(top = 2.dp))
            Text(
                text = appInfo.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
