package com.example.taskreminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    state: HomeState,
    onTitleChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onTimeChanged: (String) -> Unit,
    onTimerMinutesChanged: (String) -> Unit,
    onUseTimerChanged: (Boolean) -> Unit,
    onReminderEnabledChanged: (Boolean) -> Unit,
    onAddTask: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val now = remember { java.time.LocalDateTime.now() }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "TaskReminder",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "Bir tarihe görev atayıp takip edebileceğin basit bir hatırlatıcı.",
            style = MaterialTheme.typography.bodyMedium,
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Yeni görev",
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = state.titleInput,
                    onValueChange = onTitleChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Görev") },
                    singleLine = true,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sayaç modu",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Dakika ver, süre bitince hatırlat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.useTimer,
                        onCheckedChange = onUseTimerChanged,
                    )
                }

                AnimatedVisibility(visible = state.useTimer) {
                    OutlinedTextField(
                        value = state.timerMinutesInput,
                        onValueChange = onTimerMinutesChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Sayaç (dakika)") },
                        placeholder = { Text("30") },
                        singleLine = true,
                    )
                }

                AnimatedVisibility(visible = !state.useTimer) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = state.dateInput,
                            onValueChange = onDateChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Tarih") },
                            placeholder = { Text(dateFormatter.format(now.toLocalDate())) },
                            singleLine = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                )
                            },
                        )

                        FilledTonalButton(
                            onClick = {
                                val initial = runCatching { LocalDate.parse(state.dateInput, dateFormatter) }.getOrNull()
                                    ?: LocalDate.now()
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val picked = LocalDate.of(y, m + 1, d)
                                        onDateChanged(dateFormatter.format(picked))
                                    },
                                    initial.year,
                                    initial.monthValue - 1,
                                    initial.dayOfMonth,
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Tarih seç")
                        }

                        OutlinedTextField(
                            value = state.timeInput,
                            onValueChange = onTimeChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Saat") },
                            placeholder = { Text(timeFormatter.format(now.toLocalTime())) },
                            singleLine = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                )
                            },
                        )

                        FilledTonalButton(
                            onClick = {
                                val initial = runCatching { LocalTime.parse(state.timeInput, timeFormatter) }.getOrNull()
                                    ?: LocalTime.now()
                                TimePickerDialog(
                                    context,
                                    { _, hh, mm ->
                                        onTimeChanged(timeFormatter.format(LocalTime.of(hh, mm)))
                                    },
                                    initial.hour,
                                    initial.minute,
                                    true,
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Saat seç")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Bildirimle hatırlat", style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = state.reminderEnabled,
                        onCheckedChange = onReminderEnabledChanged,
                    )
                }

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Button(
                    onClick = onAddTask,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Kaydet")
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilledTonalButton(
                    onClick = onNavigateToList,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Liste")
                }

                FilledTonalButton(
                    onClick = onNavigateToCalendar,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Takvim")
                }

                FilledTonalButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Ayarlar")
                }
            }
        }

        Text(
            text = "İpucu: Görev eklerken sayaç modu ile süre bitiminde veya tarih+saat ile planlı hatırlatma alabilirsin.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Start,
        )
    }
}
