package com.burhannexus.dukaandost.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.burhannexus.dukaandost.R
import com.burhannexus.dukaandost.core.LabelSheet
import com.burhannexus.dukaandost.domain.model.Product
import com.burhannexus.dukaandost.ui.trade.printPdf
import com.burhannexus.dukaandost.ui.trade.sharePdf
import kotlinx.coroutines.launch
import java.io.File

/**
 * Asks how many barcodes to print for [product] and turns the answer into an A4 PDF of small labels
 * that can be shared or sent straight to a printer.
 */
@Composable
fun BarcodeLabelsDialog(
    product: Product,
    onCreate: suspend (copies: Int, cutLines: Boolean) -> BarcodePdf,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Most shops want one label for every piece in stock.
    var copiesText by remember {
        mutableStateOf(product.stock.toBigInteger().toInt().coerceIn(1, LabelSheet.MAX_COPIES).toString())
    }
    var cutLines by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    val copies = copiesText.trim().toIntOrNull()?.takeIf { it in 1..LabelSheet.MAX_COPIES }
    val title = stringResource(R.string.barcode_dialog_title) + " - " + product.name

    fun create(then: (File) -> Unit) {
        val count = copies ?: return
        busy = true
        failed = false
        scope.launch {
            when (val result = onCreate(count, cutLines)) {
                is BarcodePdf.Ready -> {
                    busy = false
                    then(result.file)
                    onDismiss()
                }
                BarcodePdf.Failed -> {
                    busy = false
                    failed = true
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(R.string.barcode_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = product.barcode?.let { stringResource(R.string.barcode_existing_note, it) }
                        ?: stringResource(R.string.barcode_new_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = copiesText,
                    onValueChange = { copiesText = it.filter(Char::isDigit).take(4); failed = false },
                    label = { Text(stringResource(R.string.barcode_copies)) },
                    singleLine = true,
                    isError = copies == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (copies == null) {
                    Text(
                        stringResource(R.string.barcode_err_copies, LabelSheet.MAX_COPIES),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        stringResource(
                            R.string.barcode_pages_info,
                            copies,
                            LabelSheet.pageCount(copies),
                            LabelSheet.PER_PAGE
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.barcode_cut_lines), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = cutLines, onCheckedChange = { cutLines = it })
                }
                if (failed) {
                    Text(
                        stringResource(R.string.barcode_err_failed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(enabled = copies != null && !busy, onClick = { create { file -> sharePdf(context, file, title) } }) {
                    Text(stringResource(R.string.barcode_share))
                }
                TextButton(enabled = copies != null && !busy, onClick = { create { file -> printPdf(context, file, title) } }) {
                    Text(stringResource(R.string.barcode_print))
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !busy, onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
