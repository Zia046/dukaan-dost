package com.burhannexus.dukaandost.domain.repository

import com.burhannexus.dukaandost.domain.model.DateRange
import com.burhannexus.dukaandost.domain.model.ReportData
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun observe(range: DateRange): Flow<ReportData>
}
