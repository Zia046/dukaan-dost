package com.burhannexus.dukaandost.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burhannexus.dukaandost.domain.model.DashboardPeriod
import com.burhannexus.dukaandost.domain.model.DateRange
import com.burhannexus.dukaandost.domain.model.DateRangeResolver
import com.burhannexus.dukaandost.domain.model.ReportData
import com.burhannexus.dukaandost.domain.repository.ReportRepository
import com.burhannexus.dukaandost.ui.dashboard.DashboardFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/** [data] is null until the first result of the current filter arrives. */
data class ReportsUiState(
    val filter: DashboardFilter,
    val range: DateRange,
    val data: ReportData?
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: ReportRepository
) : ViewModel() {

    private val filter = MutableStateFlow(DashboardFilter(period = DashboardPeriod.THIS_MONTH))

    val uiState: StateFlow<ReportsUiState> = filter
        .flatMapLatest { f ->
            val range = DateRangeResolver.resolve(f.period, LocalDate.now(), f.customStart, f.customEnd)
            repository.observe(range).map { data -> ReportsUiState(f, range, data) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardFilter(period = DashboardPeriod.THIS_MONTH).let { f ->
                ReportsUiState(f, DateRangeResolver.resolve(f.period, LocalDate.now()), null)
            }
        )

    fun selectPeriod(period: DashboardPeriod) {
        filter.value = DashboardFilter(period = period)
    }

    fun selectCustomRange(start: LocalDate, end: LocalDate) {
        filter.value = DashboardFilter(DashboardPeriod.CUSTOM, start, end)
    }
}
