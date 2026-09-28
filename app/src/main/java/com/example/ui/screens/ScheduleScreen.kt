package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecurrenceType
import com.example.data.model.ScheduleEntity
import com.example.data.model.ScheduleType
import com.example.ui.theme.*
import com.example.util.DateTimeUtils
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    schedules: List<ScheduleEntity>,
    onAddSchedule: (ScheduleType, String, Long, RecurrenceType, Boolean, String) -> Unit,
    onDeleteSchedule: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val next7Days = remember { DateTimeUtils.getNext7Days() }
    var selectedDayIndex by remember { mutableIntStateOf(0) } // 0 is today

    val selectedDay = next7Days.getOrNull(selectedDayIndex)

    // Filter schedules for the selected day or recurring matching
    val filteredSchedules = remember(schedules, selectedDay) {
        if (selectedDay == null) return@remember emptyList()
        schedules.filter { s ->
            val sDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(s.dateTimeMillis), DateTimeUtils.ZONE_ID).toLocalDate()
            when (s.recurrence) {
                RecurrenceType.SEKALI -> sDate.isEqual(selectedDay.localDate)
                RecurrenceType.SETIAP_HARI -> true
                RecurrenceType.HARI_YANG_SAMA_SETIAP_MINGGU -> sDate.dayOfWeek == selectedDay.localDate.dayOfWeek
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
                icon = { Icon(Icons.Default.Add, contentDescription = "Tambah") },
                text = { Text("Buat Jadwal / Acara", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_schedule")
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
                        text = "Jadwal & Agenda",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Pantau jadwal hari ini dan 7 hari ke depan dengan alarm otomatis",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 7 Days Timeline Strip (Hari ini dan 7 hari ke depan)
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(next7Days.indices.toList()) { index ->
                        val dayItem = next7Days[index]
                        val isSelected = index == selectedDayIndex

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) BluePrimary else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                            modifier = Modifier
                                .width(78.dp)
                                .clickable { selectedDayIndex = index }
                                .testTag("day_strip_item_$index")
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (index == 0) "Hari ini" else dayItem.dayName.take(3),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = dayItem.dateFormatted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Current Selected Day Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedDayIndex == 0) "Jadwal Hari Ini" else "Jadwal ${selectedDay?.dayName} (${selectedDay?.dateFormatted})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        Text(
                            text = "${filteredSchedules.size} agenda terdaftar",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (selectedDayIndex == 0) {
                        Surface(
                            color = GreenSuccessContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Sedang Berjalan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // List of schedules
            if (filteredSchedules.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = BlueLight,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tidak ada jadwal untuk hari ini.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Waktu luang atau silakan buat jadwal aktivitas baru.",
                                fontSize = 11.sp,
                                color = TextSecondaryLight
                            )
                        }
                    }
                }
            } else {
                items(filteredSchedules, key = { it.id }) { schedule ->
                    ScheduleItemCard(
                        schedule = schedule,
                        onDelete = { onDeleteSchedule(schedule.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddDialog) {
        AddScheduleDialog(
            defaultLocalDate = selectedDay?.localDate ?: LocalDate.now(),
            onDismiss = { showAddDialog = false },
            onSave = { type, title, time, recurrence, alarm, notes ->
                onAddSchedule(type, title, time, recurrence, alarm, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ScheduleItemCard(
    schedule: ScheduleEntity,
    onDelete: () -> Unit
) {
    val isActivity = schedule.type == ScheduleType.AKTIVITAS
    val badgeColor = if (isActivity) BluePrimary else OrangeWarning
    val badgeBg = if (isActivity) BlueLight else OrangeContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActivity) Icons.Default.DirectionsRun else Icons.Default.Celebration,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = schedule.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(color = badgeBg, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = if (isActivity) "Aktivitas" else "Acara/Kegiatan",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = TextSecondaryLight, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = DateTimeUtils.formatTime(schedule.dateTimeMillis),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BluePrimary
                    )

                    if (schedule.recurrence != RecurrenceType.SEKALI) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Repeat, contentDescription = null, tint = TextSecondaryLight, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (schedule.recurrence == RecurrenceType.SETIAP_HARI) "Setiap Hari" else "Hari yang Sama",
                            fontSize = 10.sp,
                            color = TextSecondaryLight
                        )
                    }
                }

                if (schedule.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = schedule.notes,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (schedule.hasAlarm) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Alarm Aktif",
                        tint = OrangeWarning,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus",
                        tint = TextSecondaryLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddScheduleDialog(
    defaultLocalDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (ScheduleType, String, Long, RecurrenceType, Boolean, String) -> Unit
) {
    val context = LocalContext.current
    var type by remember { mutableStateOf(ScheduleType.AKTIVITAS) }
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf(RecurrenceType.SEKALI) }
    var hasAlarm by remember { mutableStateOf(true) }

    var selectedYear by remember { mutableIntStateOf(defaultLocalDate.year) }
    var selectedMonth by remember { mutableIntStateOf(defaultLocalDate.monthValue) }
    var selectedDay by remember { mutableIntStateOf(defaultLocalDate.dayOfMonth) }
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Buat Jadwal Baru", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                // Type selector: Jadwal Aktivitas vs Acara/Kegiatan
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == ScheduleType.AKTIVITAS,
                        onClick = { type = ScheduleType.AKTIVITAS },
                        label = { Text("Aktivitas Harian") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == ScheduleType.ACARA_KEGIATAN,
                        onClick = { type = ScheduleType.ACARA_KEGIATAN },
                        label = { Text("Acara / Kegiatan") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (type == ScheduleType.AKTIVITAS) "Nama Aktivitas" else "Nama Acara/Kegiatan") },
                    placeholder = { Text(if (type == ScheduleType.AKTIVITAS) "contoh: Kuliah Algoritma" else "contoh: Rapat BEM / Hangout") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date & Time pickers
                Text(text = "Waktu & Tanggal:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    selectedYear = y
                                    selectedMonth = m + 1
                                    selectedDay = d
                                },
                                selectedYear,
                                selectedMonth - 1,
                                selectedDay
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$selectedDay/$selectedMonth/$selectedYear", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, min ->
                                    selectedHour = h
                                    selectedMinute = min
                                },
                                selectedHour,
                                selectedMinute,
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(String.format("%02d:%02d WIB", selectedHour, selectedMinute), fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pengulangan Hari
                Text(text = "Pengulangan Hari:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = recurrence == RecurrenceType.SEKALI,
                        onClick = { recurrence = RecurrenceType.SEKALI },
                        label = { Text("Sekali", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = recurrence == RecurrenceType.HARI_YANG_SAMA_SETIAP_MINGGU,
                        onClick = { recurrence = RecurrenceType.HARI_YANG_SAMA_SETIAP_MINGGU },
                        label = { Text("Hari Sama", fontSize = 10.sp) },
                        modifier = Modifier.weight(1.2f)
                    )
                    FilterChip(
                        selected = recurrence == RecurrenceType.SETIAP_HARI,
                        onClick = { recurrence = RecurrenceType.SETIAP_HARI },
                        label = { Text("Tiap Hari", fontSize = 10.sp) },
                        modifier = Modifier.weight(1.1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Alarm switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Pengingat Alarm & Notifikasi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Bunyikan alarm di luar aplikasi saat waktu tiba", fontSize = 10.sp, color = TextSecondaryLight)
                    }
                    Switch(
                        checked = hasAlarm,
                        onCheckedChange = { hasAlarm = it },
                        modifier = Modifier.testTag("schedule_alarm_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Tambahan (opsional)") },
                    placeholder = { Text("contoh: Bawa tugas laporan lab") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val millis = DateTimeUtils.combineDateAndTime(
                            selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute
                        )
                        onSave(type, title, millis, recurrence, hasAlarm, notes)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Simpan Jadwal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
