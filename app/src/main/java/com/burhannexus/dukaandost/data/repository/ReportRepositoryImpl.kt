package com.burhannexus.dukaandost.data.repository

import com.burhannexus.dukaandost.core.Minor
import com.burhannexus.dukaandost.data.local.MoneyPoint
import com.burhannexus.dukaandost.data.local.dao.ExpenseDao
import com.burhannexus.dukaandost.data.local.dao.ReportDao
import com.burhannexus.dukaandost.domain.calc.DatedAmount
import com.burhannexus.dukaandost.domain.calc.DatedSale
import com.burhannexus.dukaandost.domain.calc.ProductSale
import com.burhannexus.dukaandost.domain.calc.ReportCalculator
import com.burhannexus.dukaandost.domain.model.DateRange
import com.burhannexus.dukaandost.domain.model.ExpenseCategory
import com.burhannexus.dukaandost.domain.model.ReportData
import com.burhannexus.dukaandost.domain.repository.ReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Figures follow the dashboard definitions: sales are net of sales returns, gross profit = sales - cost of goods,
 * net profit = gross profit - expenses. Sales and returns are dated by when they happened.
 */
@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportDao: ReportDao,
    private val expenseDao: ExpenseDao
) : ReportRepository {

    override fun observe(range: DateRange): Flow<ReportData> {
        val zone = ZoneId.systemDefault()
        val start = range.start.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = range.end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        return combine(
            reportDao.observeSales(start, end),
            reportDao.observeReturns(start, end),
            expenseDao.observeRange(start, end),
            reportDao.observeSoldProducts(start, end),
            reportDao.observeReturnedProducts(start, end)
        ) { sales, returns, expenses, sold, returned ->
            fun MoneyPoint.day(): LocalDate = Instant.ofEpochMilli(createdAt).atZone(zone).toLocalDate()

            val points = sales.map { DatedSale(it.day(), it.totalMinor, it.cogsMinor) } +
                returns.map { DatedSale(it.day(), -it.totalMinor, -it.cogsMinor) }
            val datedExpenses = expenses.map {
                DatedAmount(Instant.ofEpochMilli(it.spentAt).atZone(zone).toLocalDate(), it.amountMinor)
            }
            val granularity = ReportCalculator.granularityFor(range)

            ReportData(
                range = range,
                granularity = granularity,
                summary = ReportCalculator.summary(
                    invoices = sales.size,
                    sales = points,
                    returnsMinor = returns.sumOf { it.totalMinor },
                    expensesMinor = expenses.sumOf { it.amountMinor }
                ),
                trend = ReportCalculator.trend(range, granularity, points, datedExpenses),
                expenseCategories = ReportCalculator.expenseCategories(
                    expenses.map {
                        (runCatching { ExpenseCategory.valueOf(it.category) }.getOrDefault(ExpenseCategory.OTHER)) to it.amountMinor
                    }
                ),
                topProducts = ReportCalculator.topProducts(
                    sold = sold.map { ProductSale(it.productId, it.productName, it.quantityMilli, it.salesMinor) },
                    returnedMilli = returned.associate { it.productId to it.quantityMilli }
                )
            )
        }.flowOn(Dispatchers.Default)
    }
}
