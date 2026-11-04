package com.burhannexus.dukaandost.ui.expenses

import androidx.annotation.StringRes
import com.burhannexus.dukaandost.R
import com.burhannexus.dukaandost.core.Minor
import com.burhannexus.dukaandost.domain.model.ExpenseCategory
import com.burhannexus.dukaandost.domain.model.ExpenseInput
import com.burhannexus.dukaandost.domain.model.ExpenseLimits
import com.burhannexus.dukaandost.domain.model.PaymentMethod
import com.burhannexus.dukaandost.ui.products.FormError
import com.burhannexus.dukaandost.ui.products.ProductValidator
import java.time.LocalDate

/** Raw text of the add/edit expense dialog. */
data class ExpenseForm(
    val title: String = "",
    val amount: String = "",
    val note: String = "",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val method: PaymentMethod = PaymentMethod.CASH,
    val date: LocalDate = LocalDate.now()
)

enum class ExpenseField { TITLE, AMOUNT, NOTE }

sealed interface ExpenseValidation {
    data class Valid(val input: ExpenseInput) : ExpenseValidation
    data class Invalid(val errors: Map<ExpenseField, FormError>) : ExpenseValidation
}

object ExpenseValidator {
    fun validate(id: Long, form: ExpenseForm): ExpenseValidation {
        val errors = linkedMapOf<ExpenseField, FormError>()

        val title = form.title.trim()
        if (title.isEmpty()) errors[ExpenseField.TITLE] = FormError.REQUIRED
        else if (title.length > ExpenseLimits.MAX_TITLE) errors[ExpenseField.TITLE] = FormError.TOO_LONG

        if (form.note.trim().length > ExpenseLimits.MAX_NOTE) errors[ExpenseField.NOTE] = FormError.TOO_LONG

        val amount = ProductValidator.parseDecimal(form.amount, 2, Minor.MAX_MONEY)
        if (form.amount.isBlank()) errors[ExpenseField.AMOUNT] = FormError.REQUIRED
        else if (amount == null || amount.signum() <= 0) errors[ExpenseField.AMOUNT] = FormError.INVALID_NUMBER

        if (errors.isNotEmpty() || amount == null) return ExpenseValidation.Invalid(errors)
        return ExpenseValidation.Valid(
            ExpenseInput(
                id = id,
                title = title,
                category = form.category,
                amount = amount,
                method = form.method,
                note = form.note.trim(),
                spentOn = form.date
            )
        )
    }
}

@StringRes
fun ExpenseCategory.expenseLabelRes(): Int = when (this) {
    ExpenseCategory.RENT -> R.string.expense_cat_rent
    ExpenseCategory.SALARY -> R.string.expense_cat_salary
    ExpenseCategory.ELECTRICITY -> R.string.expense_cat_electricity
    ExpenseCategory.TRANSPORT -> R.string.expense_cat_transport
    ExpenseCategory.FOOD -> R.string.expense_cat_food
    ExpenseCategory.MAINTENANCE -> R.string.expense_cat_maintenance
    ExpenseCategory.MARKETING -> R.string.expense_cat_marketing
    ExpenseCategory.OTHER -> R.string.expense_cat_other
}
