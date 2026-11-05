package com.burhannexus.dukaandost.ui.parties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burhannexus.dukaandost.data.sms.SmsResult
import com.burhannexus.dukaandost.data.sms.SmsSender
import com.burhannexus.dukaandost.domain.calc.ReminderMessage
import com.burhannexus.dukaandost.domain.repository.SmsTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val templates: SmsTemplateRepository,
    private val sender: SmsSender
) : ViewModel() {

    val template: StateFlow<String> = templates.template
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReminderMessage.DEFAULT_TEMPLATE)

    fun hasPermission(): Boolean = sender.hasPermission()

    fun saveTemplate(text: String) {
        viewModelScope.launch { templates.save(text) }
    }

    fun resetTemplate() {
        viewModelScope.launch { templates.reset() }
    }

    fun send(phone: String, message: String): SmsResult = sender.send(phone, message)
}
