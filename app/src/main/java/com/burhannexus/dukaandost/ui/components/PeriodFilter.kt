package com.burhannexus.dukaandost.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.burhannexus.dukaandost.R
import com.burhannexus.dukaandost.domain.model.DashboardPeriod

@StringRes
fun DashboardPeriod.periodLabelRes(): Int = when (this) {
    DashboardPeriod.TODAY -> R.string.period_today
    DashboardPeriod.YESTERDAY -> R.string.period_yesterday
    DashboardPeriod.THIS_WEEK -> R.string.period_this_week
    DashboardPeriod.THIS_MONTH -> R.string.period_this_month
    DashboardPeriod.THIS_YEAR -> R.string.period_this_year
    DashboardPeriod.CUSTOM -> R.string.period_custom
}

/** Row of period chips shared by Expenses and Reports. Choosing Custom is reported through [onSelect] too. */
@Composable
fun PeriodFilterRow(
    selected: DashboardPeriod,
    onSelect: (DashboardPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(DashboardPeriod.entries.toList()) { period ->
            FilterChip(
                selected = selected == period,
                onClick = { onSelect(period) },
                label = { Text(stringResource(period.periodLabelRes())) }
            )
        }
    }
}
