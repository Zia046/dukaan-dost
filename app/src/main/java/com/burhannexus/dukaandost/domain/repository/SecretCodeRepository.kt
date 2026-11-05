package com.burhannexus.dukaandost.domain.repository

import kotlinx.coroutines.flow.Flow

interface SecretCodeRepository {
    /** The 10-letter mapping; the default one until the owner changes it. */
    val mapping: Flow<String>

    /** @return false when [mapping] is not 10 distinct letters. */
    suspend fun saveMapping(mapping: String): Boolean
}
