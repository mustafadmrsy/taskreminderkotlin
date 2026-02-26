package com.example.taskreminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID

enum class ThemeMode {
    System,
    Light,
    Dark,
}

data class TaskItem(
    val id: String,
    val title: String,
    val createdAt: LocalDateTime,
    val triggerAt: LocalDateTime,
    val timerMinutes: Long?,
    val focusMinutes: Long?,
    val focusEnabled: Boolean,
    val reminderEnabled: Boolean,
    val isDone: Boolean,
)

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColorEnabled: Boolean = true,
)

data class HomeState(
    val titleInput: String = "",
    val dateInput: String = "",
    val timeInput: String = "",
    val timerMinutesInput: String = "",
    val useTimer: Boolean = false,
    val reminderEnabled: Boolean = true,
    val focusEnabled: Boolean = false,
    val focusMinutesInput: String = "",
    val errorMessage: String? = null,
)

data class MainState(
    val tasks: List<TaskItem> = emptyList(),
    val settings: SettingsState = SettingsState(),
    val home: HomeState = HomeState(),
)

sealed interface UiEvent {
    data class ScheduleReminder(
        val taskId: String,
        val title: String,
        val triggerAt: LocalDateTime,
        val focusDurationMillis: Long,
    ) : UiEvent

    data class StartFocusNow(
        val focusDurationMillis: Long,
    ) : UiEvent
}

class MainViewModel : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val _state = MutableStateFlow(
        MainState(
            tasks = listOf(
                TaskItem(
                    id = UUID.randomUUID().toString(),
                    title = "Örnek: 10dk yürüyüş",
                    createdAt = LocalDateTime.now(),
                    triggerAt = LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 30)),
                    timerMinutes = null,
                    focusMinutes = null,
                    focusEnabled = false,
                    reminderEnabled = true,
                    isDone = false,
                ),
                TaskItem(
                    id = UUID.randomUUID().toString(),
                    title = "Örnek: Fatura ödeme",
                    createdAt = LocalDateTime.now(),
                    triggerAt = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(9, 0)),
                    timerMinutes = null,
                    focusMinutes = null,
                    focusEnabled = false,
                    reminderEnabled = true,
                    isDone = false,
                ),
            )
        )
    )
    val state: StateFlow<MainState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()

    fun onTitleChanged(value: String) {
        _state.value = _state.value.copy(home = _state.value.home.copy(titleInput = value, errorMessage = null))
    }

    fun onDateChanged(value: String) {
        _state.value = _state.value.copy(home = _state.value.home.copy(dateInput = value, errorMessage = null))
    }

    fun onTimeChanged(value: String) {
        _state.value = _state.value.copy(home = _state.value.home.copy(timeInput = value, errorMessage = null))
    }

    fun onTimerMinutesChanged(value: String) {
        _state.value = _state.value.copy(home = _state.value.home.copy(timerMinutesInput = value, errorMessage = null))
    }

    fun setUseTimer(enabled: Boolean) {
        _state.value = _state.value.copy(home = _state.value.home.copy(useTimer = enabled, errorMessage = null))
    }

    fun setReminderEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(home = _state.value.home.copy(reminderEnabled = enabled))
    }

    fun setFocusEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(home = _state.value.home.copy(focusEnabled = enabled))
    }

    fun onFocusMinutesChanged(value: String) {
        _state.value = _state.value.copy(home = _state.value.home.copy(focusMinutesInput = value, errorMessage = null))
    }

    fun addTaskFromInputs() {
        val current = _state.value
        val title = current.home.titleInput.trim()
        if (title.isBlank()) {
            _state.value = current.copy(home = current.home.copy(errorMessage = "Görev başlığı boş olamaz."))
            return
        }

        val home = current.home

        val createdAt = LocalDateTime.now()
        val timerMinutes: Long?

        val focusMinutes: Long?
        val focusDurationMillis: Long
        if (home.focusEnabled) {
            val minutes = home.focusMinutesInput.trim().toLongOrNull()
            if (minutes == null || minutes <= 0) {
                _state.value = current.copy(home = home.copy(errorMessage = "Odak süresi dakika değeri 0'dan büyük olmalı."))
                return
            }
            focusMinutes = minutes
            focusDurationMillis = minutes * 60_000L
        } else {
            focusMinutes = null
            focusDurationMillis = 0L
        }

        val triggerAt = if (home.useTimer) {
            val minutes = home.timerMinutesInput.trim().toLongOrNull()
            if (minutes == null || minutes <= 0) {
                _state.value = current.copy(home = home.copy(errorMessage = "Sayaç dakika değeri 0'dan büyük olmalı."))
                return
            }
            timerMinutes = minutes
            createdAt.plusMinutes(minutes)
        } else {
            timerMinutes = null
            val dateText = home.dateInput.trim()
            val timeText = home.timeInput.trim()

            val parsedDate = runCatching { LocalDate.parse(dateText, dateFormatter) }.getOrNull()
            if (parsedDate == null) {
                _state.value = current.copy(home = home.copy(errorMessage = "Tarih formatı yyyy-aa-gg olmalı (örn: 2026-02-25)."))
                return
            }

            val parsedTime = try {
                LocalTime.parse(timeText, timeFormatter)
            } catch (_: DateTimeParseException) {
                null
            }
            if (parsedTime == null) {
                _state.value = current.copy(home = home.copy(errorMessage = "Saat formatı HH:mm olmalı (örn: 09:30)."))
                return
            }

            LocalDateTime.of(parsedDate, parsedTime)
        }

        val newTask = TaskItem(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = createdAt,
            triggerAt = triggerAt,
            timerMinutes = timerMinutes,
            focusMinutes = focusMinutes,
            focusEnabled = home.focusEnabled,
            reminderEnabled = home.reminderEnabled,
            isDone = false,
        )

        _state.value = current.copy(
            tasks = (listOf(newTask) + current.tasks).sortedBy { it.triggerAt },
            home = HomeState(),
        )

        if (home.focusEnabled && focusDurationMillis > 0L) {
            viewModelScope.launch {
                _events.emit(UiEvent.StartFocusNow(focusDurationMillis))
            }
        }

        if (newTask.reminderEnabled || newTask.focusEnabled) {
            viewModelScope.launch {
                _events.emit(
                    UiEvent.ScheduleReminder(
                        taskId = newTask.id,
                        title = newTask.title,
                        triggerAt = newTask.triggerAt,
                        focusDurationMillis = focusDurationMillis,
                    )
                )
            }
        }
    }

    fun toggleDone(taskId: String) {
        val current = _state.value
        _state.value = current.copy(
            tasks = current.tasks.map { task ->
                if (task.id == taskId) task.copy(isDone = !task.isDone) else task
            }
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        val current = _state.value
        _state.value = current.copy(settings = current.settings.copy(themeMode = mode))
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        val current = _state.value
        _state.value = current.copy(settings = current.settings.copy(dynamicColorEnabled = enabled))
    }
}
