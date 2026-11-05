package com.burhannexus.dukaandost.domain.repository

import kotlinx.coroutines.flow.Flow

interface SmsTemplateRepository {
    /** The reminder text; the standard one until the owner changes it. */
    val template: Flow<String>

    suspend fun save(template: String)

    suspend fun reset()
}
