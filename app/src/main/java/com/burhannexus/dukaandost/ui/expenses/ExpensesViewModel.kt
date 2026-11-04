package com.burhannexus.dukaandost.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burhannexus.dukaandost.domain.model.DashboardPeriod
import com.burhannexus.dukaandost.domain.model.DateRange
import com.burhannexus.dukaandost.domain.model.DateRangeResolver
import com.burhannexus.dukaandost.domain.model.Expense
import com.burhannexus.dukaandost.domain.model.ExpenseInput
import com.burhannexus.dukaandost.domain.model.ExpenseSaveResult
import com.burhannexus.dukaandost.domain.repository.ExpenseRepository
import com.burhannexus.dukaandost.ui.dashboard.DashboardFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

data class ExpensesUiState(
    val filter: DashboardFilter,
    val range: DateRange,
    val items: List<Expense>,
    val total: BigDecimal
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val filter = MutableStateFlow(DashboardFilter(period = DashboardPeriod.THIS_MONTH))

    val uiState: StateFlow<ExpensesUiState> = filter
        .flatMapLatest { f ->
            val range = DateRangeResolver.resolve(f.period, LocalDate.now(), f.customStart, f.customEnd)
            repository.observe(range).map { items ->
                ExpensesUiState(f, range, items, items.fold(BigDecimal.ZERO) { acc, e -> acc.add(e.amount) })
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardFilter(period = DashboardPeriod.THIS_MONTH).let { f ->
                ExpensesUiState(f, DateRangeResolver.resolve(f.period, LocalDate.now()), emptyList(), BigDecimal.ZERO)
            }
        )

    fun selectPeriod(period: DashboardPeriod) {
        filter.value = DashboardFilter(period = period)
    }

    fun selectCustomRange(start: LocalDate, end: LocalDate) {
        filter.value = DashboardFilter(DashboardPeriod.CUSTOM, start, end)
    }

    fun save(input: ExpenseInput, onResult: (ExpenseSaveResult) -> Unit) {
        viewModelScope.launch { onResult(repository.save(input)) }
    }

    fun delete(id: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.delete(id)
            onDone()
        }
    }
}
