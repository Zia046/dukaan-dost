package com.burhannexus.dukaandost.domain.calc

import com.burhannexus.dukaandost.core.Minor
import com.burhannexus.dukaandost.domain.model.CategoryShare
import com.burhannexus.dukaandost.domain.model.DateRange
import com.burhannexus.dukaandost.domain.model.ExpenseCategory
import com.burhannexus.dukaandost.domain.model.ReportGranularity
import com.burhannexus.dukaandost.domain.model.ReportSummary
import com.burhannexus.dukaandost.domain.model.TopProduct
import com.burhannexus.dukaandost.domain.model.TrendBucket
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** A sale on a day. A sales return is passed as the same thing with negative amounts. */
data class DatedSale(val date: LocalDate, val salesMinor: Long, val costMinor: Long)

data class DatedAmount(val date: LocalDate, val amountMinor: Long)

data class ProductSale(val productId: Long, val name: String, val quantityMilli: Long, val salesMinor: Long)

/** Pure report maths on paisa/milli integers, so it can be unit tested without a database. */
object ReportCalculator {
    const val MAX_DAILY_DAYS = 31
    const val TOP_PRODUCTS = 5

    fun granularityFor(range: DateRange): ReportGranularity {
        val days = ChronoUnit.DAYS.between(range.start, range.end) + 1
        return if (days <= MAX_DAILY_DAYS) ReportGranularity.DAY else ReportGranularity.MONTH
    }

    private fun bucketStart(date: LocalDate, granularity: ReportGranularity): LocalDate =
        if (granularity == ReportGranularity.DAY) date else date.withDayOfMonth(1)

    /** One bucket per day/month of the range, including empty ones. Net profit = sales - cost of goods - expenses. */
    fun trend(
        range: DateRange,
        granularity: ReportGranularity,
        sales: List<DatedSale>,
        expenses: List<DatedAmount>
    ): List<TrendBucket> {
        val starts = generateSequence(bucketStart(range.start, granularity)) {
            if (granularity == ReportGranularity.DAY) it.plusDays(1) else it.plusMonths(1)
        }.takeWhile { !it.isAfter(range.end) }.toList()

        val salesByBucket = sales.groupBy { bucketStart(it.date, granularity) }
        val expensesByBucket = expenses.groupBy { bucketStart(it.date, granularity) }

        return starts.map { start ->
            val s = salesByBucket[start].orEmpty()
            val salesMinor = s.sumOf { it.salesMinor }
            val costMinor = s.sumOf { it.costMinor }
            val expenseMinor = expensesByBucket[start].orEmpty().sumOf { it.amountMinor }
            TrendBucket(
                start = start,
                sales = Minor.minorToMoney(salesMinor),
                expenses = Minor.minorToMoney(expenseMinor),
                netProfit = Minor.minorToMoney(salesMinor - costMinor - expenseMinor)
            )
        }
    }

    fun summary(
        invoices: Int,
        sales: List<DatedSale>,
        returnsMinor: Long,
        expensesMinor: Long
    ): ReportSummary {
        val salesMinor = sales.sumOf { it.salesMinor }
        val gross = salesMinor - sales.sumOf { it.costMinor }
        return ReportSummary(
            invoices = invoices,
            sales = Minor.minorToMoney(salesMinor),
            returns = Minor.minorToMoney(returnsMinor),
            grossProfit = Minor.minorToMoney(gross),
            expenses = Minor.minorToMoney(expensesMinor),
            netProfit = Minor.minorToMoney(gross - expensesMinor)
        )
    }

    /** Biggest category first. Percentages are rounded to whole numbers, so they may not add up to exactly 100. */
    fun expenseCategories(items: List<Pair<ExpenseCategory, Long>>): List<CategoryShare> {
        val totals = items.groupBy({ it.first }, { it.second }).mapValues { it.value.sum() }
        val grand = totals.values.sum()
        if (grand <= 0L) return emptyList()
        return totals.entries
            .sortedWith(compareByDescending<Map.Entry<ExpenseCategory, Long>> { it.value }.thenBy { it.key.ordinal })
            .map { (category, amount) ->
                CategoryShare(category, Minor.minorToMoney(amount), ((amount * 100 + grand / 2) / grand).toInt())
            }
    }

    /**
     * Best sellers by item sales amount. Quantity is reduced by what was returned in the same period;
     * products whose quantity is fully returned are left out.
     */
    fun topProducts(
        sold: List<ProductSale>,
        returnedMilli: Map<Long, Long>,
        limit: Int = TOP_PRODUCTS
    ): List<TopProduct> = sold
        .map { it to (it.quantityMilli - (returnedMilli[it.productId] ?: 0L)).coerceAtLeast(0L) }
        .filter { (_, netQuantity) -> netQuantity > 0L }
        .sortedWith(compareByDescending<Pair<ProductSale, Long>> { it.first.salesMinor }.thenBy { it.first.name })
        .take(limit)
        .map { (product, netQuantity) ->
            TopProduct(product.name, Minor.milliToQuantity(netQuantity), Minor.minorToMoney(product.salesMinor))
        }
}
