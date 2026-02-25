package com.example.taskreminder

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.net.Uri
import android.provider.Settings
import android.app.AlarmManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.taskreminder.ui.theme.TaskReminderTheme
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import java.time.Duration
import java.time.ZoneId

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskReminderApp()
        }
    }
}

@Composable
private fun TaskReminderApp(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { },
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .setData(Uri.parse("package:${context.packageName}"))
                context.startActivity(intent)
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.ScheduleReminder -> {
                    val nowInstant = java.time.Instant.now()
                    val triggerInstant = event.triggerAt.atZone(ZoneId.systemDefault()).toInstant()
                    val delayMillis = Duration.between(nowInstant, triggerInstant).toMillis()

                    val scheduledWithAlarm = AlarmScheduler.scheduleExact(
                        context = context,
                        taskId = event.taskId,
                        title = event.title,
                        triggerAtEpochMillis = triggerInstant.toEpochMilli(),
                    )

                    if (!scheduledWithAlarm) {
                        ReminderScheduler.scheduleReminder(
                            context = context,
                            taskId = event.taskId,
                            title = event.title,
                            delayMillis = delayMillis,
                        )
                    }

                    val debugText = if (scheduledWithAlarm) {
                        "Planlandı (Exact): ${event.title}"
                    } else {
                        "Planlandı (Fallback): ${event.title}"
                    }
                    ReminderScheduler.showInstantPreviewNotification(context, debugText)
                }
            }
        }
    }

    val isDarkTheme = when (state.settings.themeMode) {
        ThemeMode.System -> null
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    TaskReminderTheme(
        darkTheme = isDarkTheme,
        dynamicColor = state.settings.dynamicColorEnabled,
    ) {
        val navController = rememberNavController()
        Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppRoute.Home.route,
            ) {
                composable(AppRoute.Home.route) {
                    HomeScreen(
                        state = state.home,
                        onTitleChanged = viewModel::onTitleChanged,
                        onDateChanged = viewModel::onDateChanged,
                        onTimeChanged = viewModel::onTimeChanged,
                        onTimerMinutesChanged = viewModel::onTimerMinutesChanged,
                        onUseTimerChanged = viewModel::setUseTimer,
                        onReminderEnabledChanged = viewModel::setReminderEnabled,
                        onAddTask = viewModel::addTaskFromInputs,
                        onNavigateToList = { navController.navigate(AppRoute.List.route) },
                        onNavigateToCalendar = { navController.navigate(AppRoute.Calendar.route) },
                        onNavigateToSettings = { navController.navigate(AppRoute.Settings.route) },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(AppRoute.List.route) {
                    ListScreen(
                        tasks = state.tasks,
                        onToggleDone = viewModel::toggleDone,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(AppRoute.Calendar.route) {
                    CalendarScreen(
                        tasks = state.tasks,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(AppRoute.Settings.route) {
                    SettingsScreen(
                        state = state.settings,
                        onThemeModeChanged = viewModel::setThemeMode,
                        onDynamicColorChanged = viewModel::setDynamicColorEnabled,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    TaskReminderTheme(darkTheme = false, dynamicColor = false) {
        Text("Preview")
    }
}

private sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object List : AppRoute("list")
    data object Calendar : AppRoute("calendar")
    data object Settings : AppRoute("settings")
}