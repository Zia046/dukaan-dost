package com.burhannexus.dukaandost.domain.repository

import com.burhannexus.dukaandost.domain.model.DateRange
import com.burhannexus.dukaandost.domain.model.Expense
import com.burhannexus.dukaandost.domain.model.ExpenseInput
import com.burhannexus.dukaandost.domain.model.ExpenseSaveResult
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    /** Expenses whose date falls inside [range] (inclusive), newest first. */
    fun observe(range: DateRange): Flow<List<Expense>>

    suspend fun save(input: ExpenseInput): ExpenseSaveResult

    /** Returns false when the expense no longer exists. */
    suspend fun delete(id: Long): Boolean
}
