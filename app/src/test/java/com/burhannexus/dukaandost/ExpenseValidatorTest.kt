package com.burhannexus.dukaandost

import com.burhannexus.dukaandost.domain.model.ExpenseCategory
import com.burhannexus.dukaandost.domain.model.PaymentMethod
import com.burhannexus.dukaandost.ui.expenses.ExpenseField
import com.burhannexus.dukaandost.ui.expenses.ExpenseForm
import com.burhannexus.dukaandost.ui.expenses.ExpenseValidation
import com.burhannexus.dukaandost.ui.expenses.ExpenseValidator
import com.burhannexus.dukaandost.ui.products.FormError
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseValidatorTest {
    private val day = LocalDate.of(2026, 11, 5)
    private val valid = ExpenseForm(title = "Shop rent", amount = "25000", date = day)

    private fun errorsOf(form: ExpenseForm) =
        (ExpenseValidator.validate(0, form) as ExpenseValidation.Invalid).errors

    @Test fun validFormProducesInput() {
        val form = valid.copy(
            title = "  Shop rent ",
            amount = "25000.50",
            note = " November ",
            category = ExpenseCategory.RENT,
            method = PaymentMethod.BANK
        )
        val input = (ExpenseValidator.validate(7, form) as ExpenseValidation.Valid).input
        assertEquals(7L, input.id)
        assertEquals("Shop rent", input.title)
        assertEquals(BigDecimal("25000.50"), input.amount)
        assertEquals("November", input.note)
        assertEquals(ExpenseCategory.RENT, input.category)
        assertEquals(PaymentMethod.BANK, input.method)
        assertEquals(day, input.spentOn)
    }

    @Test fun titleIsRequired() =
        assertEquals(FormError.REQUIRED, errorsOf(valid.copy(title = "   "))[ExpenseField.TITLE])

    @Test fun titleTooLong() =
        assertEquals(FormError.TOO_LONG, errorsOf(valid.copy(title = "x".repeat(81)))[ExpenseField.TITLE])

    @Test fun titleAtLimitIsAccepted() =
        assertTrue(ExpenseValidator.validate(0, valid.copy(title = "x".repeat(80))) is ExpenseValidation.Valid)

    @Test fun noteTooLong() =
        assertEquals(FormError.TOO_LONG, errorsOf(valid.copy(note = "n".repeat(301)))[ExpenseField.NOTE])

    @Test fun amountIsRequired() =
        assertEquals(FormError.REQUIRED, errorsOf(valid.copy(amount = ""))[ExpenseField.AMOUNT])

    @Test fun zeroAmountIsRejected() =
        assertEquals(FormError.INVALID_NUMBER, errorsOf(valid.copy(amount = "0"))[ExpenseField.AMOUNT])

    @Test fun negativeAmountIsRejected() =
        assertEquals(FormError.INVALID_NUMBER, errorsOf(valid.copy(amount = "-5"))[ExpenseField.AMOUNT])

    @Test fun lettersAreRejected() =
        assertEquals(FormError.INVALID_NUMBER, errorsOf(valid.copy(amount = "abc"))[ExpenseField.AMOUNT])

    @Test fun moreThanTwoDecimalsIsRejected() =
        assertEquals(FormError.INVALID_NUMBER, errorsOf(valid.copy(amount = "10.005"))[ExpenseField.AMOUNT])

    @Test fun amountAboveMaximumIsRejected() =
        assertEquals(FormError.INVALID_NUMBER, errorsOf(valid.copy(amount = "1000000000"))[ExpenseField.AMOUNT])

    @Test fun severalErrorsAreReportedTogether() {
        val errors = errorsOf(valid.copy(title = "", amount = "0"))
        assertEquals(setOf(ExpenseField.TITLE, ExpenseField.AMOUNT), errors.keys)
    }
}
