package com.burhannexus.dukaandost.domain.model

import java.math.BigDecimal
import java.time.LocalDate

/** Fixed list so reports can group expenses reliably. The database stores the enum name. */
enum class ExpenseCategory { RENT, SALARY, ELECTRICITY, TRANSPORT, FOOD, MAINTENANCE, MARKETING, OTHER }

data class Expense(
    val id: Long,
    val title: String,
    val category: ExpenseCategory,
    val amount: BigDecimal,
    val method: PaymentMethod,
    val note: String,
    val spentOn: LocalDate
)

/** [id] = 0 creates a new expense, anything else edits that expense. */
data class ExpenseInput(
    val id: Long,
    val title: String,
    val category: ExpenseCategory,
    val amount: BigDecimal,
    val method: PaymentMethod,
    val note: String,
    val spentOn: LocalDate
)

object ExpenseLimits {
    const val MAX_TITLE = 80
    const val MAX_NOTE = 300
}

enum class ExpenseSaveResult { SUCCESS, EMPTY_TITLE, TOO_LONG, INVALID_AMOUNT, NOT_FOUND }
