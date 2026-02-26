package com.example.taskreminder

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.focusDataStore: DataStore<Preferences> by preferencesDataStore(name = "focus_prefs")

object FocusPreferences {

    private val KeyBlockedPackages = stringSetPreferencesKey("blocked_packages")
    private val KeyFocusEndEpochMillis = longPreferencesKey("focus_end_epoch_millis")
    private val KeyFocusEnabled = booleanPreferencesKey("focus_enabled")

    fun blockedPackagesFlow(context: Context): Flow<Set<String>> {
        return context.focusDataStore.data.map { prefs ->
            prefs[KeyBlockedPackages] ?: emptySet()
        }
    }

    suspend fun setBlockedPackages(context: Context, packages: Set<String>) {
        context.focusDataStore.edit { prefs ->
            prefs[KeyBlockedPackages] = packages
        }
    }

    fun focusEndEpochMillisFlow(context: Context): Flow<Long> {
        return context.focusDataStore.data.map { prefs ->
            prefs[KeyFocusEndEpochMillis] ?: 0L
        }
    }

    fun focusEnabledFlow(context: Context): Flow<Boolean> {
        return context.focusDataStore.data.map { prefs ->
            prefs[KeyFocusEnabled] ?: false
        }
    }

    suspend fun startFocusSession(context: Context, durationMillis: Long) {
        val now = System.currentTimeMillis()
        val end = (now + durationMillis).coerceAtLeast(now)
        context.focusDataStore.edit { prefs ->
            prefs[KeyFocusEnabled] = true
            prefs[KeyFocusEndEpochMillis] = end
        }
    }

    suspend fun stopFocusSession(context: Context) {
        context.focusDataStore.edit { prefs ->
            prefs[KeyFocusEnabled] = false
            prefs[KeyFocusEndEpochMillis] = 0L
        }
    }

    fun isFocusActive(enabled: Boolean, endEpochMillis: Long, nowEpochMillis: Long = System.currentTimeMillis()): Boolean {
        return enabled && endEpochMillis > nowEpochMillis
    }
}
