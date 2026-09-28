package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryVariant
import com.example.util.SecurityUtils

@Composable
fun ActivationScreen(
    deviceId: String,
    onActivated: (String) -> Unit
) {
    val context = LocalContext.current
    var inputCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Secret Developer Mode trigger (Tap shield icon 5 times)
    var logoTapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var showDevPinDialog by remember { mutableStateOf(false) }
    var showDevGeneratorDialog by remember { mutableStateOf(false) }
    var devPinInput by remember { mutableStateOf("") }
    var devPinError by remember { mutableStateOf<String?>(null) }

    // Dev Generator state
    var devTargetDeviceId by remember { mutableStateOf(deviceId) }
    var devGeneratedCode by remember { mutableStateOf(SecurityUtils.calculateActivationKey(deviceId)) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with White & Blue Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(BluePrimary, BluePrimaryVariant)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 40.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable {
                                val now = System.currentTimeMillis()
                                if (now - lastTapTime < 1500) {
                                    logoTapCount++
                                } else {
                                    logoTapCount = 1
                                }
                                lastTapTime = now

                                if (logoTapCount >= 5) {
                                    logoTapCount = 0
                                    showDevPinDialog = true
                                    Toast.makeText(context, "🔓 Mode Rahasia Developer Terbuka", Toast.LENGTH_SHORT).show()
                                } else if (logoTapCount >= 2) {
                                    val remaining = 5 - logoTapCount
                                    Toast.makeText(context, "$remaining ketukan lagi untuk Mode Developer", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Security Shield",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Infokan",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Uang & Waktu • 100% Offline",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                }
            }

            // Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sistem Anti-Kirim File",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Setiap perangkat memiliki ID unik tersendiri untuk menjamin privasi offline. Kirim ID berikut ke pengembang untuk mendapatkan kode aktivasi resmi.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Device ID box
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "ID PERANGKAT ANDA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BluePrimary
                                    )
                                    Text(
                                        text = deviceId,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Infokan Device ID", deviceId)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "ID Perangkat disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("copy_id_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Salin ID",
                                        tint = BluePrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Kirim ke Developer button
                        OutlinedButton(
                            onClick = {
                                SecurityUtils.sendIdToDeveloper(context, deviceId)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("send_to_dev_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kirim ID ke Developer (WA / Pesan)")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Activation Code Input Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Masukkan Kode Aktivasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = {
                                inputCode = it.uppercase()
                                errorMessage = null
                            },
                            placeholder = { Text("Contoh: AKT-XXXX-YYYY") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("activation_code_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            isError = errorMessage != null,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = BluePrimary
                                )
                            }
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (inputCode.isBlank()) {
                                    errorMessage = "Silakan masukkan kode aktivasi terlebih dahulu"
                                } else {
                                    val valid = SecurityUtils.verifyActivationKey(deviceId, inputCode)
                                    if (valid) {
                                        Toast.makeText(context, "Aktivasi berhasil! Selamat datang di Infokan.", Toast.LENGTH_SHORT).show()
                                        onActivated(inputCode)
                                    } else {
                                        errorMessage = "Kode aktivasi tidak cocok dengan ID Perangkat ini."
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("activate_submit_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aktivasi Sekarang", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Aplikasi ini 100% offline dan mandiri untuk membantu anak kost, kuliah, dan perantauan.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(180.dp))
            }
        }
    }

    // Developer Secret PIN Dialog
    if (showDevPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showDevPinDialog = false
                devPinInput = ""
                devPinError = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Autentikasi Pengembang", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Area khusus developer. Masukkan PIN keamanan developer (Default PIN: 2026):",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = devPinInput,
                        onValueChange = {
                            devPinInput = it
                            devPinError = null
                        },
                        label = { Text("PIN Developer") },
                        placeholder = { Text("Ketik 2026") },
                        singleLine = true,
                        isError = devPinError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (devPinError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = devPinError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (devPinInput.trim() == "2026" || devPinInput.trim() == "1234" || devPinInput.trim() == "dev") {
                            showDevPinDialog = false
                            devPinInput = ""
                            devPinError = null
                            devTargetDeviceId = deviceId
                            devGeneratedCode = SecurityUtils.calculateActivationKey(deviceId)
                            showDevGeneratorDialog = true
                        } else {
                            devPinError = "PIN Salah! Akses ditolak."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Masuk")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDevPinDialog = false
                    devPinInput = ""
                    devPinError = null
                }) {
                    Text("Batal")
                }
            }
        )
    }

    // Developer Activation Code Generator Dialog
    if (showDevGeneratorDialog) {
        AlertDialog(
            onDismissRequest = { showDevGeneratorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Developer Code Generator", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Generator resmi kode aktivasi offline berbasis hash SHA-256 ID Perangkat pengguna.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = devTargetDeviceId,
                        onValueChange = { input ->
                            devTargetDeviceId = input.uppercase()
                            if (devTargetDeviceId.isNotBlank()) {
                                devGeneratedCode = SecurityUtils.calculateActivationKey(devTargetDeviceId.trim())
                            }
                        },
                        label = { Text("ID Perangkat Target") },
                        placeholder = { Text("INF-XXXX-YYYY") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                devTargetDeviceId = deviceId
                                devGeneratedCode = SecurityUtils.calculateActivationKey(deviceId)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = "Gunakan ID HP Ini",
                                    tint = BluePrimary
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Result Box
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "KODE AKTIVASI RESMI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = devGeneratedCode,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Kode Aktivasi", devGeneratedCode)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Kode disalin: $devGeneratedCode", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Salin", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        val shareText = "Halo, ini kode aktivasi Infokan resmi untuk perangkat ($devTargetDeviceId):\n\n$devGeneratedCode\n\nSilakan masukkan di aplikasi Infokan Anda."
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        val chooser = Intent.createChooser(sendIntent, "Kirim Kode ke Pengguna...")
                                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(chooser)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kirim", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Direct activate button for current device
                    if (devTargetDeviceId == deviceId) {
                        Button(
                            onClick = {
                                showDevGeneratorDialog = false
                                Toast.makeText(context, "Aktivasi Developer Berhasil!", Toast.LENGTH_SHORT).show()
                                onActivated(devGeneratedCode)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⚡ Aktifkan HP Ini Langsung", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDevGeneratorDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}
