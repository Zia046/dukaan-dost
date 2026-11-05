package com.burhannexus.dukaandost.ui.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burhannexus.dukaandost.data.pdf.BarcodeSheetGenerator
import com.burhannexus.dukaandost.domain.model.Product
import com.burhannexus.dukaandost.domain.model.StockMovement
import com.burhannexus.dukaandost.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface DetailState {
    object Loading : DetailState
    object Missing : DetailState
    data class Found(val product: Product) : DetailState
}

sealed interface BarcodePdf {
    data class Ready(val file: File) : BarcodePdf
    object Failed : BarcodePdf
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository,
    private val barcodeSheets: BarcodeSheetGenerator
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<Long>("productId") ?: 0L

    val state: StateFlow<DetailState> = repository.observeProduct(productId)
        .map<Product?, DetailState> { if (it == null) DetailState.Missing else DetailState.Found(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailState.Loading)

    val movements: StateFlow<List<StockMovement>> = repository.observeMovements(productId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Makes the A4 sheet of barcode labels for this product. A product without a barcode first gets a
     * shop barcode, which is saved on the product so the same label can be printed again later.
     */
    suspend fun createBarcodePdf(copies: Int, cutLines: Boolean): BarcodePdf {
        val product = (state.value as? DetailState.Found)?.product ?: return BarcodePdf.Failed
        return try {
            val code = repository.ensureBarcode(productId) ?: return BarcodePdf.Failed
            BarcodePdf.Ready(barcodeSheets.generate(product.name, code, copies, cutLines))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            BarcodePdf.Failed
        }
    }

    /** Archives the product. The screen closes itself when the product disappears. */
    fun archive() {
        viewModelScope.launch { repository.archive(productId) }
    }
}
