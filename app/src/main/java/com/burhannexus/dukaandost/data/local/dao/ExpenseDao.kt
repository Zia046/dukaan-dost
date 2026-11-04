package com.burhannexus.dukaandost.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.burhannexus.dukaandost.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(entity: ExpenseEntity): Long

    @Update
    suspend fun update(entity: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun get(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE spentAt >= :start AND spentAt < :end ORDER BY spentAt DESC, id DESC")
    fun observeRange(start: Long, end: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amountMinor), 0) FROM expenses WHERE spentAt >= :start AND spentAt < :end")
    fun observeTotal(start: Long, end: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountMinor), 0) FROM expenses")
    fun observeTotalAll(): Flow<Long>
}
