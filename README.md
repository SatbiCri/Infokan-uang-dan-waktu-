# Infokan (uang dan waktu) - Info Uang Waktu 💰⏰

> **Aplikasi pengatur keuangan untuk anak kost, kuliah dan perantau.**  
> Membantu mengelola uang saku, anggaran harian/bulanan, jadwal kuliah, catatan tugas, dan alarm bangun secara mandiri dan 100% offline.

---

## 🌟 Fitur Utama

1. **👛 Manajemen Saldo & Keuangan Mandiri**
   - Pemisahan Saldo Tunai (**Cash Dompet**) dan Saldo Rekening (**Debit / ATM**).
   - Pencatatan cepat: Pengeluaran, Pemasukan, Tarik Tunai ATM, dan Setor Tunai.
   - Ringkasan otomatis: Pengeluaran Hari Ini & Total Pengeluaran Bulan Ini.
   - Riwayat transaksi terorganisir per bulan.

2. **🎯 Penetapan Anggaran & Dynamic Progress Bar**
   - **Master Progress Bar Anggaran**: Memantau total persentase pemakaian anggaran bulanan.
   - Indikator warna dinamis: **Biru/Hijau** (Aman <75%), **Oranye** (Waspada >75%), dan **Merah** (Batas Terlampaui).
   - **Progress Bar Kategori**: Batas limit individual untuk Makanan & Minuman, Kos & Listrik, Transportasi, Kuliah & Buku, Belanja Harian, dan Hiburan.

3. **⏰ Jam & Alarm Bawaan Handphone**
   - Tampilan angka jam digital besar dengan tanggal real-time.
   - Pengaturan hari pengulangan cerdas (*Senin - Jumat*, *Setiap Hari*, dll.).
   - Suara nada dering alarm synthesizer dan getaran perangkat (*haptic vibration*).
   - Bekerja di background dengan Android `AlarmManager` & Web Audio API pada versi PWA.

4. **📅 Jadwal Kuliah & Kegiatan 7 Hari**
   - Manajemen jadwal mingguan (Senin s/d Minggu).
   - Informasi jam, ruangan/lokasi kelas, dan catatan khusus.

5. **📝 Catatan Tugas & Deadline**
   - Checklist tugas kuliah dan urusan kost.
   - Peringatan tenggat waktu (*deadline*).

6. **🔒 100% Offline & Privasi Terjaga**
   - Menggunakan **Room Database** pada Android & **localStorage / Cache API** pada PWA.
   - Berjalan tanpa memerlukan koneksi internet ataupun server pihak ketiga.

---

## 🛠️ Menjalankan & Membangun di GitHub (GitHub Actions)

Repositori ini telah dilengkapi dengan workflow CI/CD otomatis di `.github/workflows/android-build.yml`.

### Cara Build APK Otomatis di GitHub:
1. Push repositori ini ke akun GitHub Anda:
   ```bash
   git init
   git add .
   git commit -m "Initial commit Infokan (uang dan waktu)"
   git branch -M main
   git remote add origin https://github.com/<USERNAME>/<REPO_NAME>.git
   git push -u origin main
   ```
2. Buka tab **Actions** di repositori GitHub Anda.
3. GitHub Actions akan otomatis menjalankan job **Build & Test APK**:
   - Menyiapkan JDK 17 & Android SDK.
   - Memulihkan file `debug.keystore` dari base64.
   - Menjalankan unit tests (`gradle :app:testDebugUnitTest`).
   - Melakukan kompilasi file APK (`gradle :app:assembleDebug`).
4. Setelah build selesai, Anda dapat langsung mengunduh **`Infokan-Uang-Waktu-Debug-APK`** dari bagian **Artifacts** di halaman detail workflow.

---

## 🌐 Menjalankan di PWA Builder (PWABuilder.com)

Aplikasi ini memiliki versi **Progressive Web App (PWA)** lengkap di direktori `docs/` dan `pwa/` yang 100% kompatibel dengan standar **Microsoft PWABuilder**:

### Kesiapan PWA:
- ✅ **Web App Manifest (`manifest.json`)**: Lengkap dengan nama, deskripsi, tema warna `#1d4ed8`, mode `standalone`, dan shortcut.
- ✅ **Service Worker (`sw.js`)**: Caching offline otomatis, pengelolaan aset statis, dan dukungan background sync.
- ✅ **Ikon Lengkap**: Mendukung resolusi 512x512 (*maskable* & *any*), 192x192, 144x144, dan 96x96.
- ✅ **Screenshots**: Menyediakan pratinjau mobile (*narrow*) dan desktop (*wide*).

### Cara Menggunakan di PWABuilder:
1. **Aktifkan GitHub Pages**:
   - Di repositori GitHub Anda, buka menu **Settings** > **Pages**.
   - Pada bagian **Build and deployment**, pilih Source: **GitHub Actions** (workflow `.github/workflows/deploy-pwa.yml` sudah siap), **ATAU** pilih **Deploy from a branch** > branch: `main` > folder: `/docs`.
   - Simpan. URL website Anda akan aktif di: `https://<USERNAME>.github.io/<REPO_NAME>/`.
2. **Kunjungi PWABuilder**:
   - Buka [https://www.pwabuilder.com](https://www.pwabuilder.com).
   - Masukkan URL GitHub Pages Anda di kolom input, lalu klik **Start**.
   - PWABuilder akan mendeteksi skor PWA hijau (100% lolos uji manifest & service worker).
3. **Ekspor Aplikasi**:
   - Anda dapat langsung mengekspor paket:
     - 📱 **Android**: Google Play Store Package (APK / AAB via TWA).
     - 💻 **Windows**: Windows Store MSIX Package.
     - 🍎 **iOS**: Apple App Store Package.

---

## 💻 Menjalankan Secara Lokal

### Android (Android Studio):
1. Buka folder proyek di Android Studio (Koala / Ladybug atau versi terbaru).
2. Pastikan JDK diatur ke JDK 17 atau 21.
3. Jalankan perintah build Gradle:
   ```bash
   gradle :app:assembleDebug
   ```

### PWA / Web:
Cukup jalankan web server lokal di folder `docs/` atau `pwa/`:
```bash
# Menggunakan Node.js
npx serve docs

# ATAU menggunakan Python
cd docs && python3 -m http.server 8080
```
Buka browser di `http://localhost:8080`.
