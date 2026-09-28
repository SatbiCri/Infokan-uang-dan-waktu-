package com.example.ui.screens

import android.app.TimePickerDialog
import android.media.Ringtone
import android.media.RingtoneManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmEntity
import com.example.ui.theme.*
import com.example.util.DateTimeUtils
import com.example.util.NotificationHelper
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    alarms: List<AlarmEntity>,
    onAddAlarm: (Int, Int, String, String, Boolean) -> Unit,
    onToggleAlarm: (Long, Boolean) -> Unit,
    onDeleteAlarm: (Long) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    // Find nearest active alarm to show "Alarm berbunyi dalam..."
    val nextAlarmInfo = remember(alarms) {
        val activeAlarms = alarms.filter { it.isEnabled }
        if (activeAlarms.isEmpty()) null
        else {
            val nowMillis = System.currentTimeMillis()
            val upcoming = activeAlarms.map { alarm ->
                val nextMillis = NotificationHelper.calculateNextAlarmMillis(alarm.hour, alarm.minute, alarm.repeatDays)
                alarm to nextMillis
            }.filter { it.second > nowMillis }.minByOrNull { it.second }

            upcoming?.let { (alarm, nextMillis) ->
                val diffMinutes = ((nextMillis - nowMillis) / (1000 * 60)).coerceAtLeast(1)
                val hours = diffMinutes / 60
                val mins = diffMinutes % 60
                val timeStr = if (hours > 0) "$hours jam $mins menit lagi" else "$mins menit lagi"
                "${alarm.getFormattedTime()} (${alarm.label}) berbunyi dalam $timeStr"
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BluePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.AddAlarm, contentDescription = "Tambah Alarm") },
                text = { Text("Tambah Alarm", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_alarm")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Alarm Handphone",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Alarm bawaan bersuara keras, berulang setiap hari, 100% offline",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Next alarm banner
                    Surface(
                        color = if (nextAlarmInfo != null) BlueLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = if (nextAlarmInfo != null) BluePrimary else TextSecondaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = nextAlarmInfo ?: "Semua alarm sedang dinonaktifkan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (nextAlarmInfo != null) BluePrimaryVariant else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // List of Alarms
            if (alarms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(36.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessAlarms,
                                contentDescription = null,
                                tint = BlueLight,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum Ada Alarm",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tekan tombol Tambah Alarm untuk mengatur alarm bangun pagi, kuliah, atau jadwal rutin.",
                                fontSize = 12.sp,
                                color = TextSecondaryLight,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmItemCard(
                        alarm = alarm,
                        onToggle = { isEnabled -> onToggleAlarm(alarm.id, isEnabled) },
                        onDelete = { onDeleteAlarm(alarm.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddDialog) {
        AddAlarmDialog(
            onDismiss = { showAddDialog = false },
            onSave = { h, m, label, days, vib ->
                onAddAlarm(h, m, label, days, vib)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AlarmItemCard(
    alarm: AlarmEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var isTestingSound by remember { mutableStateOf(false) }
    var ringtoneInstance by remember { mutableStateOf<Ringtone?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            ringtoneInstance?.stop()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (alarm.isEnabled) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = alarm.getFormattedTime(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alarm.isEnabled) BluePrimary else TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WIB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alarm.isEnabled) BluePrimaryVariant else TextSecondaryLight,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Text(
                        text = alarm.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else TextSecondaryLight
                    )
                }

                // Switch
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BluePrimary
                    ),
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day indicators
            val selectedDays = remember(alarm.repeatDays) {
                alarm.repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            }
            val dayNames = listOf("S", "S", "R", "K", "J", "S", "M")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..7) {
                        val isDayActive = selectedDays.contains(i)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDayActive && alarm.isEnabled) BluePrimary
                                    else if (isDayActive) TextSecondaryLight.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNames[i - 1],
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDayActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Test sound preview button
                    IconButton(
                        onClick = {
                            if (isTestingSound) {
                                ringtoneInstance?.stop()
                                isTestingSound = false
                            } else {
                                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                                val ringtone = RingtoneManager.getRingtone(context, uri)
                                ringtoneInstance = ringtone
                                ringtone?.play()
                                isTestingSound = true
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isTestingSound) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Cek Bunyi",
                            tint = if (isTestingSound) RedExpense else BluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Alarm",
                            tint = TextSecondaryLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Pengulangan: ${alarm.getRepeatDaysLabel()}",
                fontSize = 11.sp,
                color = TextSecondaryLight
            )
        }
    }
}

@Composable
fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onSave: (Int, Int, String, String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val now = remember { LocalTime.now() }
    var selectedHour by remember { mutableIntStateOf(now.hour) }
    var selectedMinute by remember { mutableIntStateOf((now.minute + 1) % 60) }
    var label by remember { mutableStateOf("Bangun Pagi") }
    var vibrate by remember { mutableStateOf(true) }

    // Day toggles (1 = Senin .. 7 = Minggu)
    val dayMap = remember {
        mutableStateMapOf(
            1 to true,
            2 to true,
            3 to true,
            4 to true,
            5 to true,
            6 to false,
            7 to false
        )
    }

    val dayNames = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Atur Alarm Baru", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = androidx.compose.ui.Modifier) {
                // Big digital clock clicker
                Card(
                    colors = CardDefaults.cardColors(containerColor = BlueLight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    selectedHour = h
                                    selectedMinute = m
                                },
                                selectedHour,
                                selectedMinute,
                                true
                            ).show()
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%02d:%02d", selectedHour, selectedMinute),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BluePrimary
                        )
                        Text(
                            text = "Tekan untuk ubah waktu (Format 24 Jam)",
                            fontSize = 11.sp,
                            color = BluePrimaryVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Nama / Label Alarm") },
                    placeholder = { Text("contoh: Kuliah Pagi, Olahraga, Evaluasi Kas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ulangi Hari:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 1..7) {
                        val isChecked = dayMap[i] == true
                        FilterChip(
                            selected = isChecked,
                            onClick = { dayMap[i] = !isChecked },
                            label = { Text(dayNames[i - 1], fontSize = 10.sp) },
                            modifier = Modifier.weight(1f).padding(horizontal = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Getar HP", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { vibrate = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val activeDays = dayMap.filter { it.value }.keys.sorted().joinToString(",")
                    onSave(selectedHour, selectedMinute, label.ifBlank { "Alarm" }, activeDays, vibrate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Simpan Alarm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
