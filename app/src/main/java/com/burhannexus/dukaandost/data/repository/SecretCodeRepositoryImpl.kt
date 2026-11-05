package com.burhannexus.dukaandost.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.burhannexus.dukaandost.domain.calc.SecretCode
import com.burhannexus.dukaandost.domain.repository.SecretCodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecretCodeRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SecretCodeRepository {

    override val mapping: Flow<String> = dataStore.data
        .map { SecretCode.validMappingOrDefault(it[KEY]) }
        .distinctUntilChanged()

    override suspend fun saveMapping(mapping: String): Boolean {
        if (!SecretCode.isValidMapping(mapping)) return false
        dataStore.edit { it[KEY] = mapping }
        return true
    }

    private companion object {
        val KEY = stringPreferencesKey("secret_code_mapping")
    }
}
