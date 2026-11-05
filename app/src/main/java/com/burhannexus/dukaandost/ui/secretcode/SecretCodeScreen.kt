package com.burhannexus.dukaandost.ui.secretcode

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burhannexus.dukaandost.R
import com.burhannexus.dukaandost.domain.calc.CodeError
import com.burhannexus.dukaandost.domain.calc.CodeResult
import com.burhannexus.dukaandost.domain.calc.SecretCode
import java.util.Locale

private const val DEV_NAME = "Burhan Nexus"
private const val DEV_PHONE = "03475093446"
private val DeepBlue = Color(0xFF0B2E5C)
private val Gold = Color(0xFFFFC107)

@Composable
fun SecretCodeScreen(
    contentPadding: PaddingValues,
    viewModel: SecretCodeViewModel = hiltViewModel()
) {
    val mapping by viewModel.mapping.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(false) }
    BackHandler(enabled = editing) { editing = false }

    Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Column(modifier = Modifier.weight(1f)) {
            if (editing) {
                CodeEditor(
                    current = mapping,
                    onSave = { viewModel.saveMapping(it) { editing = false } },
                    onCancel = { editing = false }
                )
            } else {
                Converter(mapping = mapping, onEdit = { editing = true })
            }
        }
        DeveloperFooter()
    }
}

@Composable
private fun Converter(mapping: String, onEdit: () -> Unit) {
    var decodeMode by remember { mutableStateOf(true) }
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<CodeResult?>(null) }

    fun run() {
        result = if (decodeMode) SecretCode.decode(input, mapping) else SecretCode.generate(input, mapping)
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = decodeMode,
                onClick = { decodeMode = true; input = ""; result = null },
                label = { Text(stringResource(R.string.secret_mode_decode)) }
            )
            FilterChip(
                selected = !decodeMode,
                onClick = { decodeMode = false; input = ""; result = null },
                label = { Text(stringResource(R.string.secret_mode_generate)) }
            )
        }

        OutlinedTextField(
            value = input,
            onValueChange = { new ->
                input = if (decodeMode) {
                    new.uppercase(Locale.ROOT).take(SecretCode.MAX_INPUT_LENGTH)
                } else {
                    new.filter { it in '0'..'9' }.take(SecretCode.MAX_INPUT_LENGTH)
                }
                result = null
            },
            label = {
                Text(stringResource(if (decodeMode) R.string.secret_input_code else R.string.secret_input_price))
            },
            singleLine = true,
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 24.sp),
            keyboardOptions = KeyboardOptions(
                capitalization = if (decodeMode) KeyboardCapitalization.Characters else KeyboardCapitalization.None,
                keyboardType = if (decodeMode) KeyboardType.Text else KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { run() }, modifier = Modifier.weight(1f)) {
                Text(stringResource(if (decodeMode) R.string.secret_decode else R.string.secret_generate))
            }
            OutlinedButton(onClick = { input = ""; result = null }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.secret_clear))
            }
        }

        val current = result
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(if (decodeMode) R.string.secret_result_price else R.string.secret_result_code),
                    style = MaterialTheme.typography.labelLarge
                )
                when (current) {
                    is CodeResult.Ok -> Text(
                        current.value,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = if (decodeMode) FontFamily.Default else FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    is CodeResult.Error -> Text(
                        errorText(current),
                        color = MaterialTheme.colorScheme.error
                    )
                    null -> Text(" ", fontSize = 30.sp)
                }
            }
        }

        TextButton(onClick = onEdit) { Text(stringResource(R.string.secret_edit_code)) }
    }
}

@Composable
private fun errorText(error: CodeResult.Error): String = when (error.kind) {
    CodeError.EMPTY -> stringResource(R.string.secret_error_empty)
    CodeError.BAD_LETTER -> stringResource(R.string.secret_error_letter, error.detail)
    CodeError.BAD_DIGIT -> stringResource(R.string.secret_error_digit)
}

@Composable
private fun CodeEditor(current: String, onSave: (String) -> Unit, onCancel: () -> Unit) {
    val letters = remember { mutableStateListOf<String>().apply { current.forEach { add(it.toString()) } } }
    val duplicates = letters.filter { it.isNotEmpty() }.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
    val allFilled = letters.all { it.isNotEmpty() }
    val canSave = allFilled && duplicates.isEmpty()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.secret_editor_help), style = MaterialTheme.typography.bodyMedium)

        for (row in 0..4) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                for (index in listOf(row, row + 5)) {
                    Text("$index =", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(52.dp))
                    OutlinedTextField(
                        value = letters[index],
                        onValueChange = { raw ->
                            letters[index] = raw.filter { it in 'A'..'Z' || it in 'a'..'z' }
                                .uppercase(Locale.ROOT).takeLast(1)
                        },
                        singleLine = true,
                        isError = letters[index].isNotEmpty() && letters[index] in duplicates,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                }
            }
        }

        val message = when {
            duplicates.isNotEmpty() -> stringResource(R.string.secret_error_duplicate, duplicates.joinToString(", "))
            !allFilled -> stringResource(R.string.secret_error_incomplete)
            else -> ""
        }
        if (message.isNotEmpty()) Text(message, color = MaterialTheme.colorScheme.error)
        Text(stringResource(R.string.secret_editor_warning), style = MaterialTheme.typography.bodySmall)

        Button(onClick = { onSave(letters.joinToString("")) }, enabled = canSave, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.secret_save))
        }
        OutlinedButton(
            onClick = { for (i in 0..9) letters[i] = SecretCode.DEFAULT_MAPPING[i].toString() },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.secret_reset)) }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.secret_cancel))
        }
    }
}

/** Fixed credit line: it has no edit option anywhere in the app. */
@Composable
private fun DeveloperFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepBlue)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(stringResource(R.string.secret_developed_by), color = Color.White, fontSize = 11.sp, fontStyle = FontStyle.Italic)
            Text(DEV_NAME, color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
        }
        Text(DEV_PHONE, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
