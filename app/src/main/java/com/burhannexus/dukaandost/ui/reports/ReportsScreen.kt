package com.burhannexus.dukaandost.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burhannexus.dukaandost.R
import com.burhannexus.dukaandost.core.Money
import com.burhannexus.dukaandost.domain.model.DashboardPeriod
import com.burhannexus.dukaandost.domain.model.ReportData
import com.burhannexus.dukaandost.domain.model.ReportGranularity
import com.burhannexus.dukaandost.domain.model.TrendBucket
import com.burhannexus.dukaandost.ui.components.PeriodFilterRow
import com.burhannexus.dukaandost.ui.dashboard.CustomRangeDialog
import com.burhannexus.dukaandost.ui.expenses.expenseLabelRes
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val RangeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val DayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM")
private val MonthFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

private fun bucketLabel(start: LocalDate, granularity: ReportGranularity): String =
    start.format(if (granularity == ReportGranularity.DAY) DayFormat else MonthFormat)

/** Reports: period filter, summary, sales/expenses chart, expense categories and best-selling products. */
@Composable
fun ReportsScreen(
    contentPadding: PaddingValues,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCustomDialog by remember { mutableStateOf(false) }
    val data = state.data

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PeriodFilterRow(
                selected = state.filter.period,
                onSelect = { period ->
                    if (period == DashboardPeriod.CUSTOM) showCustomDialog = true
                    else viewModel.selectPeriod(period)
                }
            )
        }
        item {
            Text(
                text = stringResource(
                    R.string.showing_range,
                    state.range.start.format(RangeFormat),
                    state.range.end.format(RangeFormat)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (data == null) return@LazyColumn

        item { SummaryCard(data) }
        item { TrendCard(data) }
        item { ExpenseCategoriesCard(data) }
        item { TopProductsCard(data) }
        item {
            Text(
                stringResource(R.string.report_footnote),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showCustomDialog) {
        CustomRangeDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { start, end ->
                showCustomDialog = false
                viewModel.selectCustomRange(start, end)
            }
        )
    }
}

@Composable
private fun SectionCard(titleRes: Int, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(titleRes), style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun MoneyRow(label: String, amount: BigDecimal, emphasised: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = if (emphasised) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium
        )
        Text(
            Money.format(amount),
            style = if (emphasised) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SummaryCard(data: ReportData) {
    val s = data.summary
    SectionCard(R.string.report_summary) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.report_invoices), style = MaterialTheme.typography.bodyMedium)
            Text(s.invoices.toString(), style = MaterialTheme.typography.bodyMedium)
        }
        MoneyRow(stringResource(R.string.report_sales), s.sales)
        MoneyRow(stringResource(R.string.report_returns), s.returns)
        HorizontalDivider()
        MoneyRow(stringResource(R.string.report_gross_profit), s.grossProfit)
        MoneyRow(stringResource(R.string.report_expenses), s.expenses)
        HorizontalDivider()
        MoneyRow(stringResource(R.string.report_net_profit), s.netProfit, emphasised = true)
    }
}

@Composable
private fun TrendCard(data: ReportData) {
    val granularity = data.granularity
    val active = data.trend.filter { it.sales.signum() != 0 || it.expenses.signum() != 0 }
    SectionCard(R.string.report_trend_title) {
        if (active.isEmpty()) {
            EmptyNote(R.string.report_no_activity)
            return@SectionCard
        }
        Legend()
        TrendChart(
            buckets = data.trend,
            description = stringResource(R.string.report_chart_description)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(bucketLabel(data.trend.first().start, granularity), style = MaterialTheme.typography.labelSmall)
            Text(bucketLabel(data.trend.last().start, granularity), style = MaterialTheme.typography.labelSmall)
        }
        HorizontalDivider()
        TrendTableHeader()
        active.asReversed().forEach { bucket -> TrendRow(bucket, granularity) }
    }
}

@Composable
private fun Legend() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendItem(MaterialTheme.colorScheme.primary, stringResource(R.string.report_sales))
        LegendItem(MaterialTheme.colorScheme.tertiary, stringResource(R.string.report_expenses))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

/** Two bars per bucket: sales (net of returns) and expenses. Negative sales are drawn as zero. */
@Composable
private fun TrendChart(buckets: List<TrendBucket>, description: String) {
    val salesColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.tertiary
    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val max = buckets.maxOfOrNull { maxOf(it.sales.toDouble(), it.expenses.toDouble()) } ?: 0.0

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(180.dp)
            .semantics { contentDescription = description }
    ) {
        val h = size.height
        val w = size.width
        drawLine(axisColor, Offset(0f, h), Offset(w, h), strokeWidth = 2f)
        if (max <= 0.0 || buckets.isEmpty()) return@Canvas

        val slot = w / buckets.size
        val barWidth = slot * 0.34f
        val gap = slot * 0.04f
        val margin = (slot - (2 * barWidth + gap)) / 2f
        buckets.forEachIndexed { index, bucket ->
            val salesHeight = (bucket.sales.toDouble().coerceAtLeast(0.0) / max * h).toFloat()
            val expenseHeight = (bucket.expenses.toDouble().coerceAtLeast(0.0) / max * h).toFloat()
            val x = index * slot + margin
            drawRect(salesColor, Offset(x, h - salesHeight), Size(barWidth, salesHeight))
            drawRect(expenseColor, Offset(x + barWidth + gap, h - expenseHeight), Size(barWidth, expenseHeight))
        }
    }
}

@Composable
private fun TrendTableHeader() {
    Row(Modifier.fillMaxWidth()) {
        val style = MaterialTheme.typography.labelSmall
        val color = MaterialTheme.colorScheme.onSurfaceVariant
        Text("", Modifier.weight(1.1f), style = style, color = color)
        Text(stringResource(R.string.report_sales), Modifier.weight(1f), style = style, color = color, textAlign = TextAlign.End)
        Text(stringResource(R.string.report_expenses), Modifier.weight(1f), style = style, color = color, textAlign = TextAlign.End)
        Text(stringResource(R.string.report_net_profit), Modifier.weight(1f), style = style, color = color, textAlign = TextAlign.End)
    }
}

@Composable
private fun TrendRow(bucket: TrendBucket, granularity: ReportGranularity) {
    Row(Modifier.fillMaxWidth()) {
        val style = MaterialTheme.typography.bodySmall
        Text(bucketLabel(bucket.start, granularity), Modifier.weight(1.1f), style = style)
        Text(plain(bucket.sales), Modifier.weight(1f), style = style, textAlign = TextAlign.End)
        Text(plain(bucket.expenses), Modifier.weight(1f), style = style, textAlign = TextAlign.End)
        Text(plain(bucket.netProfit), Modifier.weight(1f), style = style, textAlign = TextAlign.End)
    }
}

/** Compact amount for table cells: the "Rs" prefix is left out because the card already says what it is. */
private fun plain(amount: BigDecimal): String = Money.format(amount).substringAfter(' ')

@Composable
private fun ExpenseCategoriesCard(data: ReportData) {
    SectionCard(R.string.report_expense_categories) {
        if (data.expenseCategories.isEmpty()) {
            EmptyNote(R.string.report_no_expenses)
            return@SectionCard
        }
        data.expenseCategories.forEach { share ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(share.category.expenseLabelRes()), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${Money.format(share.amount)} · ${stringResource(R.string.report_percent, share.percent)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth((share.percent / 100f).coerceIn(0.02f, 1f))
                            .height(8.dp)
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopProductsCard(data: ReportData) {
    SectionCard(R.string.report_top_products) {
        if (data.topProducts.isEmpty()) {
            EmptyNote(R.string.report_no_sales)
            return@SectionCard
        }
        data.topProducts.forEachIndexed { index, product ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("${index + 1}. ${product.name}", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    Text(
                        stringResource(R.string.report_qty_sold, Money.formatQuantity(product.quantity)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(Money.format(product.sales), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun EmptyNote(textRes: Int) {
    Text(
        stringResource(textRes),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}
