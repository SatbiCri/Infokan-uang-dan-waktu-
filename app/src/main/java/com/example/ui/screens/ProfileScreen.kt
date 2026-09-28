package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.UserAccount
import com.example.ui.theme.*
import com.example.util.SecurityUtils

@Composable
fun ProfileScreen(
    userAccount: UserAccount?,
    deviceId: String,
    onUpdateProfile: (String, Int, Boolean, Boolean, Boolean) -> Unit
) {
    val context = LocalContext.current

    var name by remember(userAccount) { mutableStateOf(userAccount?.userName ?: "Anak Kost Mandiri") }
    var selectedAvatar by remember(userAccount) { mutableIntStateOf(userAccount?.avatarId ?: 0) }
    var notifyExpense by remember(userAccount) { mutableStateOf(userAccount?.notifyExpenseSound ?: true) }
    var notifyIncome by remember(userAccount) { mutableStateOf(userAccount?.notifyIncomeSound ?: true) }
    var notifyAlarm by remember(userAccount) { mutableStateOf(userAccount?.notifyAlarmSound ?: true) }
    var showEditNameDialog by remember { mutableStateOf(false) }

    val avatars = listOf("🎒", "🎓", "💻", "🍜", "🚴", "🏠")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Pengaturan Profil & Suara",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Atur identitas, notifikasi, alarm dan sistem anti-kirim file",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(BlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatars.getOrElse(selectedAvatar) { "🎓" },
                        fontSize = 42.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { showEditNameDialog = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Nama", tint = BluePrimary, modifier = Modifier.size(16.dp))
                    }
                }

                Text(
                    text = "Pengguna Mandiri • Offline",
                    fontSize = 11.sp,
                    color = TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar Selector
                Text(
                    text = "Pilih Ikon Avatar:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    avatars.forEachIndexed { index, emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (selectedAvatar == index) BluePrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    selectedAvatar = index
                                    onUpdateProfile(name, index, notifyExpense, notifyIncome, notifyAlarm)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sound & Notifications Settings
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Akses Notifikasi & Suara Alarm",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Pengeluaran sound
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notifikasi Pengeluaran", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Mengingatkan catatan belanja & kontrol saldo", fontSize = 10.sp, color = TextSecondaryLight)
                    }
                    Switch(
                        checked = notifyExpense,
                        onCheckedChange = {
                            notifyExpense = it
                            onUpdateProfile(name, selectedAvatar, it, notifyIncome, notifyAlarm)
                        },
                        modifier = Modifier.testTag("switch_notify_expense")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                // Pemasukan sound
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notifikasi Pemasukan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Bunyi notifikasi saat uang saku/gaji bertambah", fontSize = 10.sp, color = TextSecondaryLight)
                    }
                    Switch(
                        checked = notifyIncome,
                        onCheckedChange = {
                            notifyIncome = it
                            onUpdateProfile(name, selectedAvatar, notifyExpense, it, notifyAlarm)
                        },
                        modifier = Modifier.testTag("switch_notify_income")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                // Alarm Jadwal & Tugas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Alarm Jadwal, Tugas & Anggaran Penuh", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Membunyikan alarm HP saat waktu tiba atau batas anggaran terlewati", fontSize = 10.sp, color = TextSecondaryLight)
                    }
                    Switch(
                        checked = notifyAlarm,
                        onCheckedChange = {
                            notifyAlarm = it
                            onUpdateProfile(name, selectedAvatar, notifyExpense, notifyIncome, it)
                        },
                        modifier = Modifier.testTag("switch_notify_alarm")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device Security & Anti-Kirim File Info
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sistem Anti-Kirim File & Keamanan", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ID Perangkat unik mengunci data di perangkat ini secara mandiri tanpa kirim-kirim file ke server asing. 100% offline & aman.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ID PERANGKAT:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                            Text(deviceId, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("ID Perangkat", deviceId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "ID Perangkat disalin", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = BluePrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { SecurityUtils.sendIdToDeveloper(context, deviceId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kirim ID ke Developer", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Widget Home Screen Guide Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Widgets, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Widget Layar Utama (Home Screen)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Widget Infokan hadir dengan desain abu gradien transparan membulat yang elegan:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(text = "• Keuangan: Total saldo, Cash & Debit, serta 2 transaksi terakhir", fontSize = 11.sp)
                Text(text = "• Anggaran: Status batas & penggunaan uang real-time", fontSize = 11.sp)
                Text(text = "• Jadwal: Agenda aktivitas hari ini", fontSize = 11.sp)
                Text(text = "• Tugas: Waktu mulai dan deadline tugas terdekat", fontSize = 11.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = BlueLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cara Pasang: Tahan layar utama HP Anda ➔ Pilih 'Widget' ➔ Pilih 'Infokan (Uang & Waktu)' dan letakkan di home screen.",
                        fontSize = 10.sp,
                        color = BluePrimaryVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showEditNameDialog) {
        var tempName by remember { mutableStateOf(name) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Ubah Nama Panggilan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Nama Anda") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            name = tempName
                            onUpdateProfile(tempName, selectedAvatar, notifyExpense, notifyIncome, notifyAlarm)
                        }
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Batal") }
            }
        )
    }
}
