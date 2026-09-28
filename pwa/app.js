/**
 * Infokan (uang dan waktu) - Info Uang Waktu
 * Progressive Web Application Script
 * 100% Offline Ready with localStorage Persistence & PWABuilder Support
 */

// Initial Default State
const DEFAULT_STATE = {
  balances: {
    cash: 350000,
    debit: 1250000
  },
  transactions: [
    {
      id: 1,
      type: 'EXPENSE',
      amount: 25000,
      category: 'Makanan & Minuman',
      wallet: 'CASH',
      date: 'Hari ini, 12:30',
      month: 'September 2026',
      note: 'Makan siang nasi padang'
    },
    {
      id: 2,
      type: 'EXPENSE',
      amount: 15000,
      category: 'Transportasi',
      wallet: 'CASH',
      date: 'Hari ini, 08:15',
      month: 'September 2026',
      note: 'Bensin motor ke kampus'
    },
    {
      id: 3,
      type: 'INCOME',
      amount: 1500000,
      category: 'Kiriman Orang Tua',
      wallet: 'DEBIT',
      date: '01 Sep 2026, 09:00',
      month: 'September 2026',
      note: 'Uang bulanan kost'
    }
  ],
  budgets: [
    { category: 'Makanan & Minuman', limit: 800000, spent: 485000, icon: '🍔' },
    { category: 'Kos & Listrik', limit: 600000, spent: 600000, icon: '🏠' },
    { category: 'Transportasi', limit: 200000, spent: 145000, icon: '🛵' },
    { category: 'Kuliah & Buku', limit: 250000, spent: 90000, icon: '📚' },
    { category: 'Belanja Kebutuhan', limit: 300000, spent: 220000, icon: '🛒' },
    { category: 'Hiburan / Nongkrong', limit: 150000, spent: 110000, icon: '☕' }
  ],
  alarms: [
    { id: 1, time: '05:00', label: 'Bangun Subuh & Mandi', days: ['Sen', 'Sel', 'Rab', 'Kam', 'Jum'], enabled: true, sound: 'gentle' },
    { id: 2, time: '07:30', label: 'Siap Berangkat Kuliah', days: ['Sen', 'Sel', 'Rab', 'Kam', 'Jum'], enabled: true, sound: 'digital' },
    { id: 3, time: '21:00', label: 'Evaluasi Pengeluaran Harian', days: ['Sen', 'Sel', 'Rab', 'Kam', 'Jum', 'Sab', 'Min'], enabled: true, sound: 'chime' }
  ],
  schedules: [
    { id: 1, day: 'Senin', name: 'Pemrograman Mobile (Android)', time: '08:00 - 10:30', room: 'Lab Komputer 3', note: 'Membawa laptop & charger' },
    { id: 2, day: 'Senin', name: 'Basis Data Lanjut', time: '13:00 - 15:30', room: 'Gedung F Lantai 2', note: 'Presentasi ERD' },
    { id: 3, day: 'Selasa', name: 'Kecerdasan Buatan', time: '09:00 - 11:30', room: 'Ruang Teori 101', note: 'Diskusi kelompok' },
    { id: 4, day: 'Rabu', name: 'Jaringan Komputer', time: '10:00 - 12:30', room: 'Lab CISCO', note: 'Ujian Praktikum' }
  ],
  tasks: [
    { id: 1, title: 'Tugas Laporan Praktikum Android', course: 'Pemrograman Mobile', deadline: 'Besok, 23:59', completed: false, urgent: true },
    { id: 2, title: 'Bayar Iuran Listrik & Sampah Kost', course: 'Kost', deadline: '30 Sep 2026', completed: false, urgent: false },
    { id: 3, title: 'Beli Buku Catatan & Alat Tulis', course: 'Kebutuhan', deadline: '28 Sep 2026', completed: true, urgent: false }
  ],
  userProfile: {
    name: 'Anak Kost Mandiri',
    status: 'Mahasiswa / Perantau',
    isActivated: true,
    activationKey: 'INFOKAN-KOST-2026-ACTIVE'
  }
};

// State Manager
class AppState {
  constructor() {
    const saved = localStorage.getItem('infokan_state_v1');
    if (saved) {
      try {
        this.data = JSON.parse(saved);
      } catch (e) {
        this.data = JSON.parse(JSON.stringify(DEFAULT_STATE));
      }
    } else {
      this.data = JSON.parse(JSON.stringify(DEFAULT_STATE));
      this.save();
    }
  }

  save() {
    localStorage.setItem('infokan_state_v1', JSON.stringify(this.data));
  }

  reset() {
    this.data = JSON.parse(JSON.stringify(DEFAULT_STATE));
    this.save();
  }
}

const state = new AppState();

// Web Audio API Synthesizer for Alarm Ringtones
class AlarmAudioPlayer {
  constructor() {
    this.ctx = null;
    this.intervalId = null;
  }

  init() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (AudioCtx) this.ctx = new AudioCtx();
    }
  }

  playTone(freq, type = 'sine', duration = 0.2, gainVal = 0.3) {
    this.init();
    if (!this.ctx) return;
    try {
      if (this.ctx.state === 'suspended') this.ctx.resume();
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = type;
      osc.frequency.setValueAtTime(freq, this.ctx.currentTime);
      gain.gain.setValueAtTime(gainVal, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + duration);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + duration);
    } catch (e) {
      console.warn('Audio play error:', e);
    }
  }

  playPattern(soundType = 'digital') {
    this.stop();
    this.init();
    
    if (soundType === 'digital') {
      // Classic phone digital beep
      let step = 0;
      this.intervalId = setInterval(() => {
        this.playTone(880, 'square', 0.12, 0.25);
        setTimeout(() => this.playTone(880, 'square', 0.12, 0.25), 180);
        step++;
      }, 800);
    } else if (soundType === 'gentle') {
      // Gentle waking chord
      const chords = [523.25, 659.25, 783.99, 1046.50];
      let i = 0;
      this.intervalId = setInterval(() => {
        this.playTone(chords[i % chords.length], 'sine', 0.4, 0.2);
        i++;
      }, 400);
    } else {
      // Chime bell
      this.intervalId = setInterval(() => {
        this.playTone(1200, 'triangle', 0.5, 0.3);
      }, 1000);
    }

    // Trigger physical vibration on mobile devices if supported
    if ('vibrate' in navigator) {
      navigator.vibrate([300, 200, 300, 200, 500]);
    }
  }

  stop() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }
}

const audioPlayer = new AlarmAudioPlayer();

// Format Rupiah
function formatRupiah(number) {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    maximumFractionDigits: 0
  }).format(number);
}

// UI Initialization & Renders
document.addEventListener('DOMContentLoaded', () => {
  initServiceWorker();
  initNavigation();
  initClock();
  initPWAInstallPrompt();
  
  renderFinance();
  renderAlarms();
  renderSchedules();
  renderTasks();
  renderProfile();
  
  initForms();
  startAlarmBackgroundChecker();
});

// Service Worker Registration
function initServiceWorker() {
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('./sw.js')
      .then((reg) => {
        console.log('[PWA] Service Worker registered with scope:', reg.scope);
      })
      .catch((err) => {
        console.warn('[PWA] Service Worker registration failed:', err);
      });
  }
}

// PWA Install Prompt handling (PWABuilder requirement)
let deferredPrompt;
function initPWAInstallPrompt() {
  const installBtn = document.getElementById('btn-install-app');
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault();
    deferredPrompt = e;
    if (installBtn) installBtn.style.display = 'inline-flex';
  });

  if (installBtn) {
    installBtn.addEventListener('click', async () => {
      if (deferredPrompt) {
        deferredPrompt.prompt();
        const { outcome } = await deferredPrompt.userChoice;
        console.log(`User response to install: ${outcome}`);
        deferredPrompt = null;
        installBtn.style.display = 'none';
      } else {
        alert('Untuk menginstall di HP: buka menu browser (titik tiga) lalu pilih "Tambahkan ke Layar Utama" / "Install Aplikasi".');
      }
    });
  }
}

// Navigation Tabs
function initNavigation() {
  const navItems = document.querySelectorAll('.nav-item');
  const tabs = document.querySelectorAll('.tab-content');

  navItems.forEach((btn) => {
    btn.addEventListener('click', () => {
      const targetTab = btn.getAttribute('data-tab');
      
      navItems.forEach(b => b.classList.remove('active'));
      tabs.forEach(t => t.classList.remove('active'));

      btn.classList.add('active');
      const content = document.getElementById(`tab-${targetTab}`);
      if (content) content.classList.add('active');

      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  });

  // Handle URL hash navigation (e.g. #alarm, #jadwal)
  const hash = window.location.hash.replace('#', '');
  if (hash) {
    const matchingBtn = document.querySelector(`.nav-item[data-tab="${hash}"]`);
    if (matchingBtn) matchingBtn.click();
  }
}

// Real-time Clock for Alarm Screen
function initClock() {
  const clockDigits = document.getElementById('live-clock-digits');
  const clockDate = document.getElementById('live-clock-date');

  function update() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');

    if (clockDigits) {
      clockDigits.textContent = `${hours}:${minutes}:${seconds}`;
    }

    if (clockDate) {
      const options = { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' };
      clockDate.textContent = now.toLocaleDateString('id-ID', options);
    }
  }

  update();
  setInterval(update, 1000);
}

// Render Keuangan (Finance & Budget Progress Bars)
function renderFinance() {
  // Update Balances
  document.getElementById('display-saldo-cash').textContent = formatRupiah(state.data.balances.cash);
  document.getElementById('display-saldo-debit').textContent = formatRupiah(state.data.balances.debit);

  // Calculate Today & Month Expense
  let totalExpenseMonth = 0;
  let todayExpense = 0;
  state.data.transactions.forEach(t => {
    if (t.type === 'EXPENSE') {
      totalExpenseMonth += t.amount;
      if (t.date.includes('Hari ini')) {
        todayExpense += t.amount;
      }
    }
  });

  document.getElementById('display-today-expense').textContent = formatRupiah(todayExpense);
  document.getElementById('display-month-expense').textContent = formatRupiah(totalExpenseMonth);

  // Render Master Budget Progress Bar
  let totalBudgetLimit = 0;
  let totalBudgetSpent = 0;
  state.data.budgets.forEach(b => {
    totalBudgetLimit += b.limit;
    totalBudgetSpent += b.spent;
  });

  const masterPercent = totalBudgetLimit > 0 ? Math.min(Math.round((totalBudgetSpent / totalBudgetLimit) * 100), 100) : 0;
  const masterFill = document.getElementById('master-budget-fill');
  const masterPercentText = document.getElementById('master-budget-percent');
  const masterSpentText = document.getElementById('master-budget-spent');
  const masterLimitText = document.getElementById('master-budget-limit');
  const masterRemainingText = document.getElementById('master-budget-remaining');

  if (masterFill) {
    masterFill.style.width = `${masterPercent}%`;
    masterFill.className = 'progress-bar-fill ' + (masterPercent > 90 ? 'progress-red' : (masterPercent > 75 ? 'progress-orange' : 'progress-blue'));
  }
  if (masterPercentText) masterPercentText.textContent = `${masterPercent}%`;
  if (masterSpentText) masterSpentText.textContent = formatRupiah(totalBudgetSpent);
  if (masterLimitText) masterLimitText.textContent = formatRupiah(totalBudgetLimit);
  if (masterRemainingText) {
    const remaining = totalBudgetLimit - totalBudgetSpent;
    masterRemainingText.textContent = remaining >= 0 ? `Sisa: ${formatRupiah(remaining)}` : `Over: ${formatRupiah(Math.abs(remaining))}`;
  }

  // Render Categories Budget List
  const budgetContainer = document.getElementById('category-budgets-list');
  if (budgetContainer) {
    budgetContainer.innerHTML = '';
    state.data.budgets.forEach(b => {
      const pct = b.limit > 0 ? Math.round((b.spent / b.limit) * 100) : 0;
      const colorClass = pct > 90 ? 'progress-red' : (pct > 75 ? 'progress-orange' : 'progress-blue');
      const badgeClass = pct > 90 ? 'percent-red' : (pct > 75 ? 'percent-orange' : 'percent-blue');

      const item = document.createElement('div');
      item.className = 'category-budget-item';
      item.innerHTML = `
        <div class="category-header">
          <span class="category-name">${b.icon} ${b.category}</span>
          <span class="category-percent ${badgeClass}">${pct}%</span>
        </div>
        <div class="progress-bar-container">
          <div class="progress-bar-fill ${colorClass}" style="width: ${Math.min(pct, 100)}%;"></div>
        </div>
        <div class="budget-stats">
          <span>Terpakai: ${formatRupiah(b.spent)}</span>
          <span>Batas: ${formatRupiah(b.limit)}</span>
        </div>
      `;
      budgetContainer.appendChild(item);
    });
  }

  // Render Transactions List
  const transContainer = document.getElementById('transactions-container');
  if (transContainer) {
    transContainer.innerHTML = '';
    if (state.data.transactions.length === 0) {
      transContainer.innerHTML = `<p style="text-align:center;color:var(--text-muted);padding:16px;">Belum ada riwayat transaksi.</p>`;
    } else {
      state.data.transactions.slice(0, 15).forEach((t, index) => {
        const item = document.createElement('div');
        item.className = 'transaction-item';

        let icon = '💸';
        let typeClass = 'expense';
        let prefix = '-';

        if (t.type === 'INCOME') {
          icon = '💰';
          typeClass = 'income';
          prefix = '+';
        } else if (t.type === 'TRANSFER') {
          icon = '🔄';
          typeClass = 'transfer';
          prefix = '';
        }

        item.innerHTML = `
          <div class="trans-left">
            <div class="trans-icon-box ${typeClass}">${icon}</div>
            <div class="trans-info">
              <span class="trans-title">${t.category}</span>
              <span class="trans-subtitle">${t.date} • ${t.wallet}</span>
            </div>
          </div>
          <div class="trans-right">
            <div class="trans-amount ${typeClass}">${prefix} ${formatRupiah(t.amount)}</div>
            <button class="btn-delete-trans" data-index="${index}" style="background:none;border:none;color:var(--text-muted);font-size:0.75rem;cursor:pointer;margin-top:2px;">Hapus</button>
          </div>
        `;
        transContainer.appendChild(item);
      });

      // Bind delete transaction buttons
      document.querySelectorAll('.btn-delete-trans').forEach(btn => {
        btn.addEventListener('click', (e) => {
          const idx = parseInt(e.target.getAttribute('data-index'), 10);
          if (confirm('Hapus transaksi ini?')) {
            state.data.transactions.splice(idx, 1);
            state.save();
            renderFinance();
          }
        });
      });
    }
  }
}

// Render Alarms
function renderAlarms() {
  const container = document.getElementById('alarm-list-container');
  if (!container) return;

  container.innerHTML = '';
  if (state.data.alarms.length === 0) {
    container.innerHTML = `<p style="text-align:center;color:var(--text-muted);padding:16px;">Belum ada alarm. Klik "+ Tambah Alarm" di atas.</p>`;
    return;
  }

  state.data.alarms.forEach((alarm) => {
    const card = document.createElement('div');
    card.className = 'alarm-card';
    card.innerHTML = `
      <div class="alarm-info">
        <div class="alarm-time-huge">${alarm.time}</div>
        <div class="alarm-days">${alarm.days.join(', ')}</div>
        <div class="alarm-name">${alarm.label}</div>
        <div style="margin-top: 6px; display: flex; gap: 8px;">
          <button class="btn-secondary btn-test-ring" data-sound="${alarm.sound}" style="font-size: 0.7rem; padding: 4px 8px;">🔊 Tes Dering</button>
          <button class="btn-secondary btn-delete-alarm" data-id="${alarm.id}" style="font-size: 0.7rem; padding: 4px 8px; color: var(--danger);">Hapus</button>
        </div>
      </div>
      <div>
        <label class="switch">
          <input type="checkbox" class="alarm-toggle" data-id="${alarm.id}" ${alarm.enabled ? 'checked' : ''}>
          <span class="slider"></span>
        </label>
      </div>
    `;
    container.appendChild(card);
  });

  // Toggle alarm switches
  document.querySelectorAll('.alarm-toggle').forEach(input => {
    input.addEventListener('change', (e) => {
      const id = parseInt(e.target.getAttribute('data-id'), 10);
      const target = state.data.alarms.find(a => a.id === id);
      if (target) {
        target.enabled = e.target.checked;
        state.save();
      }
    });
  });

  // Test ring sound button
  document.querySelectorAll('.btn-test-ring').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const sound = e.target.getAttribute('data-sound') || 'digital';
      audioPlayer.playPattern(sound);
      setTimeout(() => audioPlayer.stop(), 3500);
    });
  });

  // Delete alarm button
  document.querySelectorAll('.btn-delete-alarm').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const id = parseInt(e.target.getAttribute('data-id'), 10);
      state.data.alarms = state.data.alarms.filter(a => a.id !== id);
      state.save();
      renderAlarms();
    });
  });
}

// Background Checker for Alarms
let lastTriggeredMinute = '';
function startAlarmBackgroundChecker() {
  setInterval(() => {
    const now = new Date();
    const currentH = String(now.getHours()).padStart(2, '0');
    const currentM = String(now.getMinutes()).padStart(2, '0');
    const currentTimeStr = `${currentH}:${currentM}`;
    
    // Check once per minute
    if (currentTimeStr === lastTriggeredMinute) return;

    const dayNames = ['Min', 'Sen', 'Sel', 'Rab', 'Kam', 'Jum', 'Sab'];
    const currentDay = dayNames[now.getDay()];

    const triggeredAlarm = state.data.alarms.find(a => 
      a.enabled && a.time === currentTimeStr && a.days.includes(currentDay)
    );

    if (triggeredAlarm) {
      lastTriggeredMinute = currentTimeStr;
      triggerAlarmRinging(triggeredAlarm);
    }
  }, 1000);
}

// Ringing Alarm Modal popup
function triggerAlarmRinging(alarm) {
  audioPlayer.playPattern(alarm.sound || 'digital');
  
  const modal = document.getElementById('modal-alarm-ringing');
  if (modal) {
    document.getElementById('ringing-alarm-time').textContent = alarm.time;
    document.getElementById('ringing-alarm-label').textContent = alarm.label;
    modal.classList.add('active');
  }

  // Also trigger system notification if allowed
  if (Notification.permission === 'granted') {
    new Notification(`Alarm: ${alarm.time}`, {
      body: alarm.label,
      icon: './icons/icon-192.png'
    });
  }
}

// Render Jadwal (Schedules)
function renderSchedules() {
  const container = document.getElementById('schedules-container');
  if (!container) return;

  container.innerHTML = '';
  if (state.data.schedules.length === 0) {
    container.innerHTML = `<p style="text-align:center;color:var(--text-muted);padding:16px;">Belum ada jadwal tersimpan.</p>`;
    return;
  }

  state.data.schedules.forEach((item) => {
    const el = document.createElement('div');
    el.className = 'schedule-item';
    el.innerHTML = `
      <div class="schedule-header">
        <span class="schedule-time">${item.time}</span>
        <span class="schedule-day-badge">${item.day}</span>
      </div>
      <div style="font-size: 0.95rem; font-weight: 700; color: #fff; margin-bottom: 2px;">${item.name}</div>
      <div style="font-size: 0.78rem; color: var(--text-secondary);">${item.room}</div>
      ${item.note ? `<div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 4px;">📝 ${item.note}</div>` : ''}
      <button class="btn-delete-schedule" data-id="${item.id}" style="background:none;border:none;color:var(--text-muted);font-size:0.75rem;cursor:pointer;margin-top:6px;">Hapus Jadwal</button>
    `;
    container.appendChild(el);
  });

  document.querySelectorAll('.btn-delete-schedule').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const id = parseInt(e.target.getAttribute('data-id'), 10);
      state.data.schedules = state.data.schedules.filter(s => s.id !== id);
      state.save();
      renderSchedules();
    });
  });
}

// Render Catatan Tugas (Tasks)
function renderTasks() {
  const container = document.getElementById('tasks-container');
  if (!container) return;

  container.innerHTML = '';
  if (state.data.tasks.length === 0) {
    container.innerHTML = `<p style="text-align:center;color:var(--text-muted);padding:16px;">Semua tugas selesai!</p>`;
    return;
  }

  state.data.tasks.forEach((task) => {
    const el = document.createElement('div');
    el.className = 'task-item';
    el.innerHTML = `
      <input type="checkbox" class="task-checkbox" data-id="${task.id}" ${task.completed ? 'checked' : ''}>
      <div class="task-content">
        <div class="task-title ${task.completed ? 'completed' : ''}">${task.title}</div>
        <div class="task-meta">
          <span>📚 ${task.course}</span>
          <span class="${task.urgent && !task.completed ? 'deadline-urgent' : ''}">⏰ ${task.deadline}</span>
        </div>
      </div>
      <button class="btn-delete-task" data-id="${task.id}" style="background:none;border:none;color:var(--text-muted);cursor:pointer;padding:4px;">✕</button>
    `;
    container.appendChild(el);
  });

  // Toggle task completed
  document.querySelectorAll('.task-checkbox').forEach(input => {
    input.addEventListener('change', (e) => {
      const id = parseInt(e.target.getAttribute('data-id'), 10);
      const target = state.data.tasks.find(t => t.id === id);
      if (target) {
        target.completed = e.target.checked;
        state.save();
        renderTasks();
      }
    });
  });

  // Delete task
  document.querySelectorAll('.btn-delete-task').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const id = parseInt(e.target.getAttribute('data-id'), 10);
      state.data.tasks = state.data.tasks.filter(t => t.id !== id);
      state.save();
      renderTasks();
    });
  });
}

// Render Profil
function renderProfile() {
  document.getElementById('stat-total-trans').textContent = state.data.transactions.length;
  document.getElementById('stat-total-budget').textContent = state.data.budgets.length;
  document.getElementById('stat-total-alarm').textContent = state.data.alarms.length;
  document.getElementById('stat-total-tasks').textContent = state.data.tasks.filter(t => !t.completed).length;

  // PWABuilder Checklist Status Verification
  const swActive = 'serviceWorker' in navigator && navigator.serviceWorker.controller !== null;
  const swBadge = document.getElementById('pwa-check-sw');
  if (swBadge) {
    swBadge.innerHTML = swActive ? '✅ Aktif & Bekerja Offline' : '🟢 Terdaftar (Siap Cache)';
  }
}

// Modal Handlers & Forms
function initForms() {
  // Open Modals
  document.getElementById('btn-add-expense').addEventListener('click', () => openModal('modal-add-expense'));
  document.getElementById('btn-add-income').addEventListener('click', () => openModal('modal-add-income'));
  document.getElementById('btn-transfer').addEventListener('click', () => openModal('modal-transfer'));
  document.getElementById('btn-open-add-alarm').addEventListener('click', () => openModal('modal-add-alarm'));
  document.getElementById('btn-open-add-schedule').addEventListener('click', () => openModal('modal-add-schedule'));
  document.getElementById('btn-open-add-task').addEventListener('click', () => openModal('modal-add-task'));

  // Close Modals
  document.querySelectorAll('.btn-close-modal').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
    });
  });

  // Submit Expense
  document.getElementById('form-add-expense').addEventListener('submit', (e) => {
    e.preventDefault();
    const amount = parseInt(document.getElementById('expense-amount').value, 10);
    const category = document.getElementById('expense-category').value;
    const wallet = document.getElementById('expense-wallet').value;
    const note = document.getElementById('expense-note').value;

    if (amount <= 0) return alert('Masukkan nominal valid!');

    // Deduct balance
    if (wallet === 'CASH') {
      state.data.balances.cash -= amount;
    } else {
      state.data.balances.debit -= amount;
    }

    // Update budget spent if category matches
    const budget = state.data.budgets.find(b => b.category === category);
    if (budget) {
      budget.spent += amount;
    }

    // Add transaction
    state.data.transactions.unshift({
      id: Date.now(),
      type: 'EXPENSE',
      amount,
      category,
      wallet,
      date: 'Hari ini, ' + new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }),
      month: 'September 2026',
      note
    });

    state.save();
    renderFinance();
    closeAllModals();
  });

  // Submit Income
  document.getElementById('form-add-income').addEventListener('submit', (e) => {
    e.preventDefault();
    const amount = parseInt(document.getElementById('income-amount').value, 10);
    const category = document.getElementById('income-category').value;
    const wallet = document.getElementById('income-wallet').value;
    const note = document.getElementById('income-note').value;

    if (amount <= 0) return alert('Masukkan nominal valid!');

    if (wallet === 'CASH') {
      state.data.balances.cash += amount;
    } else {
      state.data.balances.debit += amount;
    }

    state.data.transactions.unshift({
      id: Date.now(),
      type: 'INCOME',
      amount,
      category,
      wallet,
      date: 'Hari ini, ' + new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }),
      month: 'September 2026',
      note
    });

    state.save();
    renderFinance();
    closeAllModals();
  });

  // Submit Transfer (Tarik / Setor Tunai)
  document.getElementById('form-transfer').addEventListener('submit', (e) => {
    e.preventDefault();
    const amount = parseInt(document.getElementById('transfer-amount').value, 10);
    const type = document.getElementById('transfer-type').value;

    if (amount <= 0) return alert('Masukkan nominal valid!');

    if (type === 'TARIK') {
      // Debit -> Cash
      if (state.data.balances.debit < amount) return alert('Saldo Debit tidak mencukupi!');
      state.data.balances.debit -= amount;
      state.data.balances.cash += amount;
      state.data.transactions.unshift({
        id: Date.now(),
        type: 'TRANSFER',
        amount,
        category: 'Tarik Tunai ATM',
        wallet: 'Debit -> Cash',
        date: 'Hari ini, ' + new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }),
        month: 'September 2026',
        note: 'Tarik tunai ke dompet'
      });
    } else {
      // Cash -> Debit
      if (state.data.balances.cash < amount) return alert('Saldo Cash tidak mencukupi!');
      state.data.balances.cash -= amount;
      state.data.balances.debit += amount;
      state.data.transactions.unshift({
        id: Date.now(),
        type: 'TRANSFER',
        amount,
        category: 'Setor Tunai Bank',
        wallet: 'Cash -> Debit',
        date: 'Hari ini, ' + new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }),
        month: 'September 2026',
        note: 'Setor tunai ke rekening'
      });
    }

    state.save();
    renderFinance();
    closeAllModals();
  });

  // Submit Add Alarm
  document.getElementById('form-add-alarm').addEventListener('submit', (e) => {
    e.preventDefault();
    const time = document.getElementById('alarm-time').value;
    const label = document.getElementById('alarm-label').value || 'Alarm Baru';
    const sound = document.getElementById('alarm-sound').value;

    const checkedDays = [];
    document.querySelectorAll('.alarm-day-check:checked').forEach(cb => {
      checkedDays.push(cb.value);
    });

    state.data.alarms.push({
      id: Date.now(),
      time,
      label,
      days: checkedDays.length > 0 ? checkedDays : ['Sen', 'Sel', 'Rab', 'Kam', 'Jum', 'Sab', 'Min'],
      enabled: true,
      sound
    });

    state.save();
    renderAlarms();
    closeAllModals();
  });

  // Submit Add Schedule
  document.getElementById('form-add-schedule').addEventListener('submit', (e) => {
    e.preventDefault();
    const day = document.getElementById('schedule-day').value;
    const name = document.getElementById('schedule-name').value;
    const time = document.getElementById('schedule-time').value;
    const room = document.getElementById('schedule-room').value;
    const note = document.getElementById('schedule-note').value;

    state.data.schedules.push({
      id: Date.now(),
      day,
      name,
      time,
      room,
      note
    });

    state.save();
    renderSchedules();
    closeAllModals();
  });

  // Submit Add Task
  document.getElementById('form-add-task').addEventListener('submit', (e) => {
    e.preventDefault();
    const title = document.getElementById('task-title').value;
    const course = document.getElementById('task-course').value;
    const deadline = document.getElementById('task-deadline').value;

    state.data.tasks.push({
      id: Date.now(),
      title,
      course,
      deadline,
      completed: false,
      urgent: false
    });

    state.save();
    renderTasks();
    closeAllModals();
  });

  // Ringing Alarm Modal controls
  document.getElementById('btn-stop-alarm').addEventListener('click', () => {
    audioPlayer.stop();
    document.getElementById('modal-alarm-ringing').classList.remove('active');
  });

  // Export JSON Backup
  document.getElementById('btn-export-data').addEventListener('click', () => {
    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(state.data, null, 2));
    const dlAnchor = document.createElement('a');
    dlAnchor.setAttribute("href", dataStr);
    dlAnchor.setAttribute("download", `infokan_backup_${Date.now()}.json`);
    dlAnchor.click();
  });

  // Reset Data to Default
  document.getElementById('btn-reset-data').addEventListener('click', () => {
    if (confirm('Yakin ingin mereset seluruh data ke setelan awal?')) {
      state.reset();
      renderFinance();
      renderAlarms();
      renderSchedules();
      renderTasks();
      renderProfile();
      alert('Data berhasil direset.');
    }
  });
}

function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.add('active');
}

function closeAllModals() {
  document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
}
