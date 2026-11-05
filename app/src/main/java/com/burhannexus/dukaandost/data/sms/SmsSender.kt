package com.burhannexus.dukaandost.data.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class SmsResult { HANDED_OFF, NO_PERMISSION, FAILED }

/** Sends a text message straight from the phone, without opening the messages app. Long or Urdu text is split automatically. */
@Singleton
class SmsSender @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED

    fun send(phone: String, text: String): SmsResult {
        if (!hasPermission()) return SmsResult.NO_PERMISSION
        val number = phone.filter { it.isDigit() || it == '+' }
        if (number.isEmpty() || text.isBlank()) return SmsResult.FAILED
        return try {
            val manager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION") SmsManager.getDefault()
            } ?: return SmsResult.FAILED
            val parts = manager.divideMessage(text)
            if (parts.size > 1) {
                manager.sendMultipartTextMessage(number, null, parts, null, null)
            } else {
                manager.sendTextMessage(number, null, text, null, null)
            }
            SmsResult.HANDED_OFF
        } catch (e: Exception) {
            SmsResult.FAILED
        }
    }
}
