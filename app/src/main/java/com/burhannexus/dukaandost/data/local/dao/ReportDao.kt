package com.burhannexus.dukaandost.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.burhannexus.dukaandost.data.local.MoneyPoint
import com.burhannexus.dukaandost.data.local.ReturnedProductRow
import com.burhannexus.dukaandost.data.local.SoldProductRow
import kotlinx.coroutines.flow.Flow

/** Read-only queries for the Reports screen. Dates are epoch milliseconds, [start] inclusive and [end] exclusive. */
@Dao
interface ReportDao {
    @Query("SELECT createdAt, totalMinor, cogsMinor FROM sales WHERE createdAt >= :start AND createdAt < :end")
    fun observeSales(start: Long, end: Long): Flow<List<MoneyPoint>>

    @Query("SELECT createdAt, valueMinor AS totalMinor, cogsMinor FROM sale_returns WHERE createdAt >= :start AND createdAt < :end")
    fun observeReturns(start: Long, end: Long): Flow<List<MoneyPoint>>

    @Query(
        """SELECT si.productId AS productId, MAX(si.productName) AS productName,
           COALESCE(SUM(si.quantityMilli), 0) AS quantityMilli, COALESCE(SUM(si.lineTotalMinor), 0) AS salesMinor
           FROM sale_items si JOIN sales s ON s.id = si.saleId
           WHERE s.createdAt >= :start AND s.createdAt < :end GROUP BY si.productId"""
    )
    fun observeSoldProducts(start: Long, end: Long): Flow<List<SoldProductRow>>

    @Query(
        """SELECT ri.productId AS productId, COALESCE(SUM(ri.quantityMilli), 0) AS quantityMilli
           FROM sale_return_items ri JOIN sale_returns r ON r.id = ri.returnId
           WHERE r.createdAt >= :start AND r.createdAt < :end GROUP BY ri.productId"""
    )
    fun observeReturnedProducts(start: Long, end: Long): Flow<List<ReturnedProductRow>>
}
