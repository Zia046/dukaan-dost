package com.burhannexus.dukaandost.domain.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ReminderMessageTest {
    @Test fun formatsWholeAmountWithSeparators() {
        assertEquals("12,500", ReminderMessage.formatAmount(BigDecimal("12500.00")))
    }

    @Test fun keepsFraction() {
        assertEquals("1,250.5", ReminderMessage.formatAmount(BigDecimal("1250.50")))
    }

    @Test fun rendersAmountInBrackets() {
        val text = ReminderMessage.render(ReminderMessage.DEFAULT_TEMPLATE, BigDecimal("7500"))
        assertTrue(text.contains("(7,500) روپے"))
        assertFalse(text.contains(ReminderMessage.AMOUNT_TOKEN))
    }

    @Test fun defaultHasToken() {
        assertTrue(ReminderMessage.hasToken(ReminderMessage.DEFAULT_TEMPLATE))
        assertFalse(ReminderMessage.hasToken("no amount here"))
    }
}
