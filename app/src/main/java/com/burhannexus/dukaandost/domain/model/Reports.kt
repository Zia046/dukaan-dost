package com.burhannexus.dukaandost.domain.model

import java.math.BigDecimal
import java.time.LocalDate

/** DAY: one bar per day (ranges up to 31 days). MONTH: one bar per calendar month. */
enum class ReportGranularity { DAY, MONTH }

/** [start] is the day (DAY) or the first day of the month (MONTH). Sales are net of sales returns. */
data class TrendBucket(
    val start: LocalDate,
    val sales: BigDecimal,
    val expenses: BigDecimal,
    val netProfit: BigDecimal
)

data class CategoryShare(
    val category: ExpenseCategory,
    val amount: BigDecimal,
    val percent: Int
)

/** [sales] is the item sales amount before invoice discount and returns; [quantity] is net of returns. */
data class TopProduct(
    val name: String,
    val quantity: BigDecimal,
    val sales: BigDecimal
)

data class ReportSummary(
    val invoices: Int,
    /** Sales after discounts and after sales returns, same definition as the dashboard. */
    val sales: BigDecimal,
    val returns: BigDecimal,
    val grossProfit: BigDecimal,
    val expenses: BigDecimal,
    val netProfit: BigDecimal
)

data class ReportData(
    val range: DateRange,
    val granularity: ReportGranularity,
    val summary: ReportSummary,
    val trend: List<TrendBucket>,
    val expenseCategories: List<CategoryShare>,
    val topProducts: List<TopProduct>
)
