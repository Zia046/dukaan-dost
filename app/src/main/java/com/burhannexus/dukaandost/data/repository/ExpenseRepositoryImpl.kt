package com.burhannexus.dukaandost.data.repository

import com.burhannexus.dukaandost.core.Minor
import com.burhannexus.dukaandost.data.local.dao.ExpenseDao
import com.burhannexus.dukaandost.data.local.entity.ExpenseEntity
import com.burhannexus.dukaandost.domain.model.DateRange
import com.burhannexus.dukaandost.domain.model.Expense
import com.burhannexus.dukaandost.domain.model.ExpenseCategory
import com.burhannexus.dukaandost.domain.model.ExpenseInput
import com.burhannexus.dukaandost.domain.model.ExpenseLimits
import com.burhannexus.dukaandost.domain.model.ExpenseSaveResult
import com.burhannexus.dukaandost.domain.model.PaymentMethod
import com.burhannexus.dukaandost.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun observe(range: DateRange): Flow<List<Expense>> {
        val zone = ZoneId.systemDefault()
        val start = range.start.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = range.end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return dao.observeRange(start, end).map { list -> list.map { it.toDomain(zone) } }
    }

    override suspend fun save(input: ExpenseInput): ExpenseSaveResult {
        val title = input.title.trim()
        if (title.isEmpty()) return ExpenseSaveResult.EMPTY_TITLE
        if (title.length > ExpenseLimits.MAX_TITLE || input.note.trim().length > ExpenseLimits.MAX_NOTE) return ExpenseSaveResult.TOO_LONG
        if (input.amount.signum() <= 0 || input.amount > Minor.MAX_MONEY) return ExpenseSaveResult.INVALID_AMOUNT

        val zone = ZoneId.systemDefault()
        val entity = ExpenseEntity(
            id = input.id,
            title = title,
            category = input.category.name,
            amountMinor = Minor.moneyToMinor(input.amount),
            paymentMethod = input.method.name,
            note = input.note.trim(),
            spentAt = input.spentOn.atStartOfDay(zone).toInstant().toEpochMilli(),
            createdAt = System.currentTimeMillis()
        )
        if (input.id == 0L) {
            dao.insert(entity)
        } else {
            val existing = dao.get(input.id) ?: return ExpenseSaveResult.NOT_FOUND
            dao.update(entity.copy(createdAt = existing.createdAt))
        }
        return ExpenseSaveResult.SUCCESS
    }

    override suspend fun delete(id: Long): Boolean = dao.delete(id) > 0

    private fun ExpenseEntity.toDomain(zone: ZoneId) = Expense(
        id = id,
        title = title,
        category = runCatching { ExpenseCategory.valueOf(category) }.getOrDefault(ExpenseCategory.OTHER),
        amount = Minor.minorToMoney(amountMinor),
        method = runCatching { PaymentMethod.valueOf(paymentMethod) }.getOrDefault(PaymentMethod.CASH),
        note = note,
        spentOn = Instant.ofEpochMilli(spentAt).atZone(zone).toLocalDate()
    )

}
