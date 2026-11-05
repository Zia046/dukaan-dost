package com.burhannexus.dukaandost

import com.burhannexus.dukaandost.domain.calc.DatedAmount
import com.burhannexus.dukaandost.domain.calc.DatedSale
import com.burhannexus.dukaandost.domain.calc.ProductSale
import com.burhannexus.dukaandost.domain.calc.ReportCalculator
import com.burhannexus.dukaandost.domain.model.DateRange
import com.burhannexus.dukaandost.domain.model.ExpenseCategory
import com.burhannexus.dukaandost.domain.model.ReportGranularity
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportCalculatorTest {
    private fun d(y: Int, m: Int, day: Int) = LocalDate.of(y, m, day)
    private fun money(s: String) = BigDecimal(s)

    // ---- granularity -------------------------------------------------------------------------

    @Test fun singleDayIsDaily() =
        assertEquals(ReportGranularity.DAY, ReportCalculator.granularityFor(DateRange(d(2026, 11, 5), d(2026, 11, 5))))

    @Test fun thirtyOneDaysIsStillDaily() =
        assertEquals(ReportGranularity.DAY, ReportCalculator.granularityFor(DateRange(d(2026, 10, 1), d(2026, 10, 31))))

    @Test fun thirtyTwoDaysSwitchesToMonthly() =
        assertEquals(ReportGranularity.MONTH, ReportCalculator.granularityFor(DateRange(d(2026, 10, 1), d(2026, 11, 1))))

    @Test fun wholeYearIsMonthly() =
        assertEquals(ReportGranularity.MONTH, ReportCalculator.granularityFor(DateRange(d(2026, 1, 1), d(2026, 12, 31))))

    // ---- trend -------------------------------------------------------------------------------

    @Test fun dailyTrendHasOneBucketPerDayAndSubtractsReturnsAndExpenses() {
        val range = DateRange(d(2026, 11, 1), d(2026, 11, 3))
        val sales = listOf(
            DatedSale(d(2026, 11, 1), 10_000, 6_000),
            DatedSale(d(2026, 11, 1), -2_000, -1_000) // a return on the same day
        )
        val expenses = listOf(DatedAmount(d(2026, 11, 2), 1_500))

        val trend = ReportCalculator.trend(range, ReportGranularity.DAY, sales, expenses)

        assertEquals(listOf(d(2026, 11, 1), d(2026, 11, 2), d(2026, 11, 3)), trend.map { it.start })
        assertEquals(money("80.00"), trend[0].sales)
        assertEquals(money("30.00"), trend[0].netProfit)
        assertEquals(money("0.00"), trend[1].sales)
        assertEquals(money("15.00"), trend[1].expenses)
        assertEquals(money("-15.00"), trend[1].netProfit)
        assertEquals(money("0.00"), trend[2].netProfit)
    }

    @Test fun monthlyTrendGroupsByCalendarMonth() {
        val range = DateRange(d(2026, 1, 15), d(2026, 3, 10))
        val sales = listOf(
            DatedSale(d(2026, 1, 20), 5_000, 3_000),
            DatedSale(d(2026, 3, 5), 7_000, 4_000)
        )
        val expenses = listOf(DatedAmount(d(2026, 2, 10), 500))

        val trend = ReportCalculator.trend(range, ReportGranularity.MONTH, sales, expenses)

        assertEquals(listOf(d(2026, 1, 1), d(2026, 2, 1), d(2026, 3, 1)), trend.map { it.start })
        assertEquals(money("50.00"), trend[0].sales)
        assertEquals(money("5.00"), trend[1].expenses)
        assertEquals(money("-5.00"), trend[1].netProfit)
        assertEquals(money("30.00"), trend[2].netProfit)
    }

    @Test fun trendIgnoresRecordsOutsideTheRange() {
        val range = DateRange(d(2026, 11, 1), d(2026, 11, 2))
        val trend = ReportCalculator.trend(
            range, ReportGranularity.DAY,
            sales = listOf(DatedSale(d(2026, 12, 1), 9_999, 0)),
            expenses = listOf(DatedAmount(d(2026, 10, 1), 9_999))
        )
        assertTrue(trend.all { it.sales.signum() == 0 && it.expenses.signum() == 0 })
        assertEquals(2, trend.size)
    }

    // ---- summary -----------------------------------------------------------------------------

    @Test fun summaryMatchesDashboardDefinitions() {
        val sales = listOf(
            DatedSale(d(2026, 11, 1), 10_000, 6_000),
            DatedSale(d(2026, 11, 2), -2_000, -1_000)
        )
        val s = ReportCalculator.summary(invoices = 1, sales = sales, returnsMinor = 2_000, expensesMinor = 1_500)

        assertEquals(1, s.invoices)
        assertEquals(money("80.00"), s.sales)
        assertEquals(money("20.00"), s.returns)
        assertEquals(money("30.00"), s.grossProfit)
        assertEquals(money("15.00"), s.expenses)
        assertEquals(money("15.00"), s.netProfit)
    }

    @Test fun emptyPeriodGivesZeros() {
        val s = ReportCalculator.summary(0, emptyList(), 0, 0)
        assertEquals(money("0.00"), s.sales)
        assertEquals(money("0.00"), s.netProfit)
    }

    // ---- expense categories ------------------------------------------------------------------

    @Test fun categoriesAreSummedSortedAndGivenPercentages() {
        val shares = ReportCalculator.expenseCategories(
            listOf(
                ExpenseCategory.RENT to 20_000L,
                ExpenseCategory.FOOD to 3_000L,
                ExpenseCategory.RENT to 5_000L,
                ExpenseCategory.OTHER to 2_000L
            )
        )
        assertEquals(listOf(ExpenseCategory.RENT, ExpenseCategory.FOOD, ExpenseCategory.OTHER), shares.map { it.category })
        assertEquals(money("250.00"), shares[0].amount)
        assertEquals(listOf(83, 10, 7), shares.map { it.percent })
    }

    @Test fun equalCategoriesKeepTheListOrder() {
        val shares = ReportCalculator.expenseCategories(
            listOf(ExpenseCategory.SALARY to 1_000L, ExpenseCategory.RENT to 1_000L)
        )
        assertEquals(listOf(ExpenseCategory.RENT, ExpenseCategory.SALARY), shares.map { it.category })
        assertEquals(listOf(50, 50), shares.map { it.percent })
    }

    @Test fun noExpensesGivesNoCategories() {
        assertTrue(ReportCalculator.expenseCategories(emptyList()).isEmpty())
    }

    // ---- top products ------------------------------------------------------------------------

    @Test fun topProductsAreRankedBySalesAndReducedByReturns() {
        val sold = listOf(
            ProductSale(1, "Soap", 10_000, 50_000),
            ProductSale(2, "Tea", 5_000, 90_000),
            ProductSale(3, "Pen", 2_000, 4_000),
            ProductSale(4, "Zip", 1_000, 4_000)
        )
        val top = ReportCalculator.topProducts(sold, returnedMilli = mapOf(1L to 3_000L, 4L to 1_000L))

        assertEquals(listOf("Tea", "Soap", "Pen"), top.map { it.name }) // Zip was fully returned
        assertEquals(0, top[1].quantity.compareTo(BigDecimal("7"))) // 10 sold - 3 returned
        assertEquals(money("500.00"), top[1].sales)
    }

    @Test fun topProductsAreLimited() {
        val sold = (1L..7L).map { ProductSale(it, "P$it", 1_000, it * 100) }
        val top = ReportCalculator.topProducts(sold, emptyMap())
        assertEquals(5, top.size)
        assertEquals("P7", top.first().name)
    }

    @Test fun equalSalesAreOrderedByName() {
        val sold = listOf(ProductSale(1, "Beta", 1_000, 100), ProductSale(2, "Alpha", 1_000, 100))
        assertEquals(listOf("Alpha", "Beta"), ReportCalculator.topProducts(sold, emptyMap()).map { it.name })
    }

    @Test fun moreReturnedThanSoldNeverGoesNegative() {
        val sold = listOf(ProductSale(1, "Soap", 1_000, 100))
        assertTrue(ReportCalculator.topProducts(sold, mapOf(1L to 5_000L)).isEmpty())
    }
}
