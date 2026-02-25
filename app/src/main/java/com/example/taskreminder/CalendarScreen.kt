package com.example.taskreminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    tasks: List<TaskItem>,
    modifier: Modifier = Modifier,
) {
    val zoneId = ZoneId.systemDefault()
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    val pickerState = rememberDatePickerState()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    var nowInstant by remember { mutableStateOf(Instant.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            nowInstant = Instant.now()
        }
    }

    LaunchedEffect(pickerState.selectedDateMillis) {
        val millis = pickerState.selectedDateMillis ?: return@LaunchedEffect
        selectedDate = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
    }

    val filtered = tasks.filter { it.triggerAt.toLocalDate() == selectedDate }.sortedBy { it.triggerAt }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Takvim",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "Seçili gün: ${formatter.format(selectedDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                DatePicker(state = pickerState, title = null, headline = null, showModeToggle = false)
            }
        }

        if (filtered.isEmpty()) {
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = null)
                    Text(
                        text = "Bu güne ait görev yok.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { task ->
                    TaskCard(task = task, nowInstant = nowInstant)
                }
            }
        }
    }
}
