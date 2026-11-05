package com.burhannexus.dukaandost.domain.calc

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** The payment reminder SMS. The text can be changed by the owner; [AMOUNT_TOKEN] is replaced by the balance. */
object ReminderMessage {
    const val AMOUNT_TOKEN = "{amount}"
    const val MAX_LENGTH = 1000

    val DEFAULT_TEMPLATE: String = listOf(
        "Burhan nexus",
        "📞 0347-5093446",
        "محترم صارف،",
        "امید ہے آپ خیریت سے ہوں گے۔",
        "آپ کے لیجر کے مطابق ($AMOUNT_TOKEN) روپے کی رقم تاحال واجب الادا ہے۔",
        "براہِ کرم ہماری درخواست ہے کہ ادائیگی جلد از جلد ممکن بنائیں تاکہ حسابات بروقت کلیئر ہو سکیں۔",
        "Easypaisa ادائیگی کی تفصیل:",
        "نام: Ziaullah",
        "نمبر: 03139271732",
        "آپ کے اعتماد اور تعاون کا تہہ دل سے شکریہ"
    ).joinToString("\n")

    fun hasToken(template: String): Boolean = template.contains(AMOUNT_TOKEN)

    /** 12500 -> "12,500"; 12500.5 -> "12,500.5". Latin digits so the amount reads the same everywhere. */
    fun formatAmount(amount: BigDecimal): String =
        DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US)).format(amount)

    fun render(template: String, amount: BigDecimal): String =
        template.replace(AMOUNT_TOKEN, formatAmount(amount))
}
