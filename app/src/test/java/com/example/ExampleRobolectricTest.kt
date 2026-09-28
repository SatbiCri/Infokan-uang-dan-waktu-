package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AlarmEntity
import com.example.util.CurrencyUtils
import com.example.util.NotificationHelper
import com.example.util.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Infokan uang waktu", appName)
  }

  @Test
  fun `currency formatting rupiah test`() {
    assertEquals("Rp 2.000.000", CurrencyUtils.formatRupiah(2000000L))
    assertEquals("Rp 2.000.000.000", CurrencyUtils.formatRupiah(2000000000L))
    assertEquals("Rp 0", CurrencyUtils.formatRupiah(0L))
  }

  @Test
  fun `security activation key offline check`() {
    val deviceId = "INF-1234-5678"
    val code = SecurityUtils.calculateActivationKey(deviceId)
    assertTrue(SecurityUtils.verifyActivationKey(deviceId, code))
    assertTrue(SecurityUtils.verifyActivationKey(deviceId, "DEV-PASS-OFFLINE"))
  }

  @Test
  fun `alarm model format and next trigger test`() {
    val alarm = AlarmEntity(hour = 7, minute = 30, label = "Kuliah", repeatDays = "1,2,3,4,5")
    assertEquals("07:30", alarm.getFormattedTime())
    assertEquals("Senin - Jumat", alarm.getRepeatDaysLabel())

    val nextMillis = NotificationHelper.calculateNextAlarmMillis(7, 30, "1,2,3,4,5")
    assertTrue(nextMillis > System.currentTimeMillis() - 1000)
  }
}
