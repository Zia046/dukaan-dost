package com.burhannexus.dukaandost.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.burhannexus.dukaandost.domain.calc.ReminderMessage
import com.burhannexus.dukaandost.domain.repository.SmsTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsTemplateRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SmsTemplateRepository {

    override val template: Flow<String> = dataStore.data
        .map { it[KEY]?.takeIf { text -> text.isNotBlank() } ?: ReminderMessage.DEFAULT_TEMPLATE }
        .distinctUntilChanged()

    override suspend fun save(template: String) {
        dataStore.edit { it[KEY] = template.take(ReminderMessage.MAX_LENGTH) }
    }

    override suspend fun reset() {
        dataStore.edit { it.remove(KEY) }
    }

    private companion object {
        val KEY = stringPreferencesKey("reminder_sms_template")
    }
}
