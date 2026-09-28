package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
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
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus
import com.example.ui.theme.*
import com.example.util.DateTimeUtils
import java.time.LocalDateTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    tasks: List<TaskEntity>,
    onAddTask: (String, String, Long, Long, Boolean) -> Unit,
    onUpdateStatus: (Long, TaskStatus) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<TaskStatus?>(null) } // null = Semua

    val filteredTasks = remember(tasks, selectedFilter) {
        if (selectedFilter == null) tasks else tasks.filter { it.status == selectedFilter }
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
                text = { Text("Tambah Tugas", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_task")
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
                        text = "Daftar Tugas & Deadline",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Pengingat otomatis waktu mulai & batas akhir bahkan saat aplikasi ditutup",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status Filter Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("Semua (${tasks.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        val count = tasks.count { it.status == TaskStatus.BELUM }
                        FilterChip(
                            selected = selectedFilter == TaskStatus.BELUM,
                            onClick = { selectedFilter = TaskStatus.BELUM },
                            label = { Text("Belum ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        val count = tasks.count { it.status == TaskStatus.SEDANG }
                        FilterChip(
                            selected = selectedFilter == TaskStatus.SEDANG,
                            onClick = { selectedFilter = TaskStatus.SEDANG },
                            label = { Text("Sedang Dikerjakan ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        val count = tasks.count { it.status == TaskStatus.SELESAI }
                        FilterChip(
                            selected = selectedFilter == TaskStatus.SELESAI,
                            onClick = { selectedFilter = TaskStatus.SELESAI },
                            label = { Text("Selesai ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Task List
            if (filteredTasks.isEmpty()) {
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
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = GreenSuccessContainer,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tidak ada tugas dalam kategori ini.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tekan tombol Tambah Tugas untuk mencatat tugas baru.",
                                fontSize = 11.sp,
                                color = TextSecondaryLight
                            )
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskItemCard(
                        task = task,
                        onUpdateStatus = { newStatus -> onUpdateStatus(task.id, newStatus) },
                        onDelete = { onDeleteTask(task.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, notes, start, deadline, alarm ->
                onAddTask(title, notes, start, deadline, alarm)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onUpdateStatus: (TaskStatus) -> Unit,
    onDelete: () -> Unit
) {
    val isDone = task.status == TaskStatus.SELESAI
    val isInProgress = task.status == TaskStatus.SEDANG
    val isPastDeadline = task.deadlineMillis < System.currentTimeMillis() && !isDone

    val statusColor = when {
        isDone -> GreenSuccess
        isInProgress -> OrangeWarning
        else -> BluePrimary
    }

    val statusBg = when {
        isDone -> GreenSuccessContainer
        isInProgress -> OrangeContainer
        else -> BlueLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isDone -> Icons.Default.CheckCircle
                                isInProgress -> Icons.Default.HourglassTop
                                else -> Icons.Default.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = task.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (task.notes.isNotBlank()) {
                            Text(
                                text = task.notes,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
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

            Spacer(modifier = Modifier.height(10.dp))

            // Start & Deadline row
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Mulai Dikerjakan:", fontSize = 9.sp, color = TextSecondaryLight)
                        Text(
                            text = DateTimeUtils.formatDateTime(task.startTimeMillis),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Deadline:", fontSize = 9.sp, color = TextSecondaryLight)
                        Text(
                            text = DateTimeUtils.formatDateTime(task.deadlineMillis),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPastDeadline) RedExpense else OrangeWarning
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Changer Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = { onUpdateStatus(TaskStatus.BELUM) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (task.status == TaskStatus.BELUM) BluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (task.status == TaskStatus.BELUM) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Belum", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { onUpdateStatus(TaskStatus.SEDANG) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (task.status == TaskStatus.SEDANG) OrangeWarning else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (task.status == TaskStatus.SEDANG) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Sedang", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { onUpdateStatus(TaskStatus.SELESAI) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (task.status == TaskStatus.SELESAI) GreenSuccess else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (task.status == TaskStatus.SELESAI) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Selesai", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (task.hasAlarm) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Alarm, contentDescription = "Alarm Aktif", tint = OrangeWarning, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Alarm On", fontSize = 10.sp, color = OrangeWarning, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Long, Long, Boolean) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var hasAlarm by remember { mutableStateOf(true) }

    val now = remember { LocalDateTime.now() }
    val tomorrow = remember { now.plusDays(1) }

    var startYear by remember { mutableIntStateOf(now.year) }
    var startMonth by remember { mutableIntStateOf(now.monthValue) }
    var startDay by remember { mutableIntStateOf(now.dayOfMonth) }
    var startHour by remember { mutableIntStateOf(now.hour) }
    var startMinute by remember { mutableIntStateOf(now.minute) }

    var deadYear by remember { mutableIntStateOf(tomorrow.year) }
    var deadMonth by remember { mutableIntStateOf(tomorrow.monthValue) }
    var deadDay by remember { mutableIntStateOf(tomorrow.dayOfMonth) }
    var deadHour by remember { mutableIntStateOf(23) }
    var deadMinute by remember { mutableIntStateOf(59) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tambah Tugas Baru", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Tugas") },
                    placeholder = { Text("contoh: Makalah Ekonomi Bab 1-3") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Kecil") },
                    placeholder = { Text("contoh: Format font Times New Roman 12 pt") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Start Time Picker
                Text(text = "Kapan Mulai Dikerjakan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    startYear = y
                                    startMonth = m + 1
                                    startDay = d
                                },
                                startYear,
                                startMonth - 1,
                                startDay
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("$startDay/$startMonth/$startYear", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, min ->
                                    startHour = h
                                    startMinute = min
                                },
                                startHour,
                                startMinute,
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(String.format("%02d:%02d WIB", startHour, startMinute), fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Deadline Picker
                Text(text = "Tenggat Waktu (Deadline):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    deadYear = y
                                    deadMonth = m + 1
                                    deadDay = d
                                },
                                deadYear,
                                deadMonth - 1,
                                deadDay
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("$deadDay/$deadMonth/$deadYear", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, min ->
                                    deadHour = h
                                    deadMinute = min
                                },
                                deadHour,
                                deadMinute,
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(String.format("%02d:%02d WIB", deadHour, deadMinute), fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Alarm switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Alarm & Notifikasi Otomatis", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Mengingatkan saat waktu mulai & batas deadline", fontSize = 10.sp, color = TextSecondaryLight)
                    }
                    Switch(
                        checked = hasAlarm,
                        onCheckedChange = { hasAlarm = it },
                        modifier = Modifier.testTag("task_alarm_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val startMillis = DateTimeUtils.combineDateAndTime(
                            startYear, startMonth, startDay, startHour, startMinute
                        )
                        val deadMillis = DateTimeUtils.combineDateAndTime(
                            deadYear, deadMonth, deadDay, deadHour, deadMinute
                        )
                        onSave(title, notes, startMillis, deadMillis, hasAlarm)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Simpan Tugas")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
