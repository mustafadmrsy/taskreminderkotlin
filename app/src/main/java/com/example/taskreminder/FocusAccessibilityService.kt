package com.example.taskreminder

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FocusAccessibilityService : AccessibilityService() {

    private val tag = "FocusAccessibility"

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var blockedPackages: Set<String> = emptySet()

    @Volatile
    private var focusEnabled: Boolean = false

    @Volatile
    private var focusEndEpochMillis: Long = 0L

    private var lastBlockedPackage: String? = null
    private var lastLaunchAt: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.d(tag, "onServiceConnected")

        serviceScope.launch {
            combine(
                FocusPreferences.blockedPackagesFlow(applicationContext),
                FocusPreferences.focusEnabledFlow(applicationContext),
                FocusPreferences.focusEndEpochMillisFlow(applicationContext),
            ) { blocked, enabled, endMillis ->
                Triple(blocked, enabled, endMillis)
            }.collect { (blocked, enabled, endMillis) ->
                blockedPackages = blocked
                focusEnabled = enabled
                focusEndEpochMillis = endMillis

                Log.d(tag, "prefs updated: blocked=${blocked.size}, enabled=$enabled, end=$endMillis")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            Log.d(tag, "event type=${event.eventType} pkg=${event.packageName}")
        }

        val packageName = resolveForegroundPackage(event) ?: return
        if (packageName == applicationContext.packageName) return

        val active = FocusPreferences.isFocusActive(
            enabled = focusEnabled,
            endEpochMillis = focusEndEpochMillis,
        )
        if (!active) {
            Log.d(tag, "focus not active; pkg=$packageName")
            return
        }

        if (!blockedPackages.contains(packageName)) {
            Log.d(tag, "not blocked; pkg=$packageName")
            return
        }

        val now = System.currentTimeMillis()
        val recentlyLaunchedSame = (lastBlockedPackage == packageName) && (now - lastLaunchAt < 750L)
        if (recentlyLaunchedSame) return
        lastBlockedPackage = packageName
        lastLaunchAt = now

        Log.d(tag, "BLOCK: launching BlockScreenActivity for pkg=$packageName")

        val intent = Intent(this, BlockScreenActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(BlockScreenActivity.ExtraBlockedPackage, packageName)

        startActivity(intent)
    }

    private fun resolveForegroundPackage(event: AccessibilityEvent): String? {
        val root: AccessibilityNodeInfo? = rootInActiveWindow
        val rootPackage = root?.packageName?.toString()
        if (!rootPackage.isNullOrBlank()) return rootPackage
        val eventPackage = event.packageName?.toString()
        if (!eventPackage.isNullOrBlank()) return eventPackage
        return null
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
