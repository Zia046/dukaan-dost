package com.burhannexus.dukaandost.ui.secretcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burhannexus.dukaandost.domain.calc.SecretCode
import com.burhannexus.dukaandost.domain.repository.SecretCodeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecretCodeViewModel @Inject constructor(
    private val repository: SecretCodeRepository
) : ViewModel() {

    val mapping: StateFlow<String> = repository.mapping
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SecretCode.DEFAULT_MAPPING)

    fun saveMapping(value: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            if (repository.saveMapping(value)) onSaved()
        }
    }
}
