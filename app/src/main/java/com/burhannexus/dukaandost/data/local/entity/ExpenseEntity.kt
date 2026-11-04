package com.burhannexus.dukaandost.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A shop running cost (rent, salary, electricity ...). Money is stored as paisa.
 * [spentAt] is the start of the day the money was spent (device time zone); [createdAt] is when it was entered.
 */
@Entity(tableName = "expenses", indices = [Index("spentAt")])
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val amountMinor: Long,
    val paymentMethod: String,
    val note: String,
    val spentAt: Long,
    val createdAt: Long
)
