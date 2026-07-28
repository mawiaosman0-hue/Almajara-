package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppUtils {

    /**
     * Formats any phone number string into international WhatsApp phone format without leading + or 0.
     * Defaults to Sudan country code (249) if no country code is present.
     */
    fun formatWhatsAppPhone(phone: String?): String {
        if (phone.isNullOrBlank()) return ""
        var digits = phone.filter { it.isDigit() }
        if (digits.isEmpty()) return ""

        // Handle 00249 prefix
        if (digits.startsWith("00249")) {
            digits = digits.substring(2)
        }

        // Handle 249 prefix
        if (digits.startsWith("249")) {
            // Fix double zero or leading zero after country code (e.g. 2490912345678 -> 249912345678)
            if (digits.length == 13 && digits.startsWith("2490")) {
                digits = "249" + digits.substring(4)
            }
            return digits
        }

        // Remove single leading zero (e.g. 0912345678 or 0123456789 -> 912345678 or 123456789)
        if (digits.startsWith("0")) {
            digits = digits.substring(1)
        }

        // Add Sudan country code 249 if not starting with 249
        if (!digits.startsWith("249") && digits.isNotEmpty()) {
            digits = "249$digits"
        }

        return digits
    }

    /**
     * Opens WhatsApp application directly with formatted phone number and pre-filled message.
     * Falls back to general intent or chooser if direct package fails.
     */
    fun sendWhatsAppMessage(context: Context, phone: String?, message: String = "") {
        val cleanPhone = formatWhatsAppPhone(phone)
        if (cleanPhone.isBlank()) {
            Toast.makeText(context, "رقم الهاتف غير متاح للواتساب ⚠️", Toast.LENGTH_SHORT).show()
            return
        }

        val encodedMsg = if (message.isNotBlank()) Uri.encode(message) else ""
        val url = if (encodedMsg.isNotBlank()) {
            "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"
        } else {
            "https://api.whatsapp.com/send?phone=$cleanPhone"
        }

        try {
            // Attempt direct launch into WhatsApp app
            val directIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.whatsapp")
                if (context !is android.app.Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(directIntent)
        } catch (e: Exception) {
            try {
                // Fallback for WhatsApp Business or default handler
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    if (context !is android.app.Activity) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                // Ultimate fallback: share intent chooser or toast
                try {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message)
                        if (context !is android.app.Activity) {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "إرسال عبر الواتساب"))
                } catch (ex: Exception) {
                    Toast.makeText(context, "الرجاء تثبيت تطبيق الواتساب على الهاتف 💬", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
