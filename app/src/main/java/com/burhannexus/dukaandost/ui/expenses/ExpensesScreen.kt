package com.burhannexus.dukaandost.ui.expenses

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burhannexus.dukaandost.R
import com.burhannexus.dukaandost.core.Money
import com.burhannexus.dukaandost.domain.model.DashboardPeriod
import com.burhannexus.dukaandost.domain.model.ExpenseCategory
import com.burhannexus.dukaandost.domain.model.Expense
import com.burhannexus.dukaandost.domain.model.ExpenseInput
import com.burhannexus.dukaandost.domain.model.ExpenseSaveResult
import com.burhannexus.dukaandost.domain.model.PaymentMethod
import com.burhannexus.dukaandost.ui.dashboard.CustomRangeDialog
import com.burhannexus.dukaandost.ui.products.FormError
import com.burhannexus.dukaandost.ui.trade.DecimalField
import com.burhannexus.dukaandost.ui.trade.labelRes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val DateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@StringRes
private fun DashboardPeriod.periodLabelRes(): Int = when (this) {
    DashboardPeriod.TODAY -> R.string.period_today
    DashboardPeriod.YESTERDAY -> R.string.period_yesterday
    DashboardPeriod.THIS_WEEK -> R.string.period_this_week
    DashboardPeriod.THIS_MONTH -> R.string.period_this_month
    DashboardPeriod.THIS_YEAR -> R.string.period_this_year
    DashboardPeriod.CUSTOM -> R.string.period_custom
}

@StringRes
private fun FormError.messageRes(): Int = when (this) {
    FormError.REQUIRED -> R.string.err_required
    FormError.INVALID_NUMBER -> R.string.err_invalid_number
    FormError.TOO_LONG -> R.string.err_too_long
    else -> R.string.err_required
}

@StringRes
private fun ExpenseSaveResult.messageRes(): Int = when (this) {
    ExpenseSaveResult.EMPTY_TITLE -> R.string.err_required
    ExpenseSaveResult.TOO_LONG -> R.string.err_too_long
    ExpenseSaveResult.INVALID_AMOUNT -> R.string.err_invalid_number
    ExpenseSaveResult.NOT_FOUND -> R.string.expense_not_found
    ExpenseSaveResult.SUCCESS -> R.string.err_required
}

/** Expenses: filter by period, see the total, add, edit and delete. */
@Composable
fun ExpensesScreen(
    contentPadding: PaddingValues,
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Expense?>(null) }
    var showCustomDialog by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DashboardPeriod.entries.toList()) { period ->
                        FilterChip(
                            selected = state.filter.period == period,
                            onClick = {
                                if (period == DashboardPeriod.CUSTOM) showCustomDialog = true
                                else viewModel.selectPeriod(period)
                            },
                            label = { Text(stringResource(period.periodLabelRes())) }
                        )
                    }
                }
            }
            item {
                Text(
                    text = stringResource(
                        R.string.showing_range,
                        state.range.start.format(DateFormat),
                        state.range.end.format(DateFormat)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.expenses_total), style = MaterialTheme.typography.bodyMedium)
                        Text(Money.format(state.total), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            if (state.items.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.expenses_empty),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(state.items, key = { it.id }) { expense ->
                    ExpenseCard(expense, onClick = { editing = expense })
                }
            }
        }
        FloatingActionButton(
            onClick = { adding = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.expense_add))
        }
    }

    if (adding) {
        ExpenseFormDialog(
            initial = null,
            onDismiss = { adding = false },
            onSave = { input, done -> viewModel.save(input, done) },
            onDelete = null
        )
    }
    editing?.let { expense ->
        ExpenseFormDialog(
            initial = expense,
            onDismiss = { editing = null },
            onSave = { input, done -> viewModel.save(input, done) },
            onDelete = { done -> viewModel.delete(expense.id, done) }
        )
    }
    if (showCustomDialog) {
        CustomRangeDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { start, end ->
                showCustomDialog = false
                viewModel.selectCustomRange(start, end)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseCard(expense: Expense, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(expense.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                Text(
                    text = "${stringResource(expense.category.expenseLabelRes())} · " +
                        "${expense.spentOn.format(DateFormat)} · ${stringResource(expense.method.labelRes())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Text(Money.format(expense.amount), style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Add (initial == null) or edit an expense. Editing also offers Delete, which asks for confirmation. */
@Composable
private fun ExpenseFormDialog(
    initial: Expense?,
    onDismiss: () -> Unit,
    onSave: (ExpenseInput, (ExpenseSaveResult) -> Unit) -> Unit,
    onDelete: ((() -> Unit) -> Unit)?
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var amount by remember { mutableStateOf(initial?.amount?.toPlainString().orEmpty()) }
    var note by remember { mutableStateOf(initial?.note.orEmpty()) }
    var category by remember { mutableStateOf(initial?.category ?: ExpenseCategory.OTHER) }
    var method by remember { mutableStateOf(initial?.method ?: PaymentMethod.CASH) }
    var date by remember { mutableStateOf(initial?.spentOn ?: LocalDate.now()) }
    var errors by remember { mutableStateOf<Map<ExpenseField, FormError>>(emptyMap()) }
    var generalError by remember { mutableStateOf<Int?>(null) }
    var saving by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var pickingDate by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(stringResource(if (initial == null) R.string.expense_add else R.string.expense_edit)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errors = errors - ExpenseField.TITLE },
                    label = { Text(stringResource(R.string.expense_title)) },
                    singleLine = true,
                    isError = ExpenseField.TITLE in errors,
                    supportingText = { errors[ExpenseField.TITLE]?.let { Text(stringResource(it.messageRes())) } },
                    modifier = Modifier.fillMaxWidth()
                )
                DecimalField(
                    value = amount,
                    onValueChange = { amount = it; errors = errors - ExpenseField.AMOUNT },
                    label = stringResource(R.string.expense_amount),
                    isError = ExpenseField.AMOUNT in errors,
                    modifier = Modifier.fillMaxWidth()
                )
                errors[ExpenseField.AMOUNT]?.let {
                    Text(stringResource(it.messageRes()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                Text(stringResource(R.string.expense_category), style = MaterialTheme.typography.labelMedium)
                Box {
                    OutlinedButton(onClick = { menuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(category.expenseLabelRes()))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        ExpenseCategory.entries.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(stringResource(c.expenseLabelRes())) },
                                onClick = { category = c; menuOpen = false }
                            )
                        }
                    }
                }
                Text(stringResource(R.string.expense_method), style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethod.entries.forEach { m ->
                        FilterChip(
                            selected = method == m,
                            onClick = { method = m },
                            label = { Text(stringResource(m.labelRes())) }
                        )
                    }
                }
                OutlinedButton(onClick = { pickingDate = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("${stringResource(R.string.expense_date)}: ${date.format(DateFormat)}")
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it; errors = errors - ExpenseField.NOTE },
                    label = { Text(stringResource(R.string.expense_note)) },
                    isError = ExpenseField.NOTE in errors,
                    supportingText = { errors[ExpenseField.NOTE]?.let { Text(stringResource(it.messageRes())) } },
                    modifier = Modifier.fillMaxWidth()
                )
                generalError?.let {
                    Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = {
                    val form = ExpenseForm(title, amount, note, category, method, date)
                    when (val outcome = ExpenseValidator.validate(initial?.id ?: 0L, form)) {
                        is ExpenseValidation.Invalid -> errors = outcome.errors
                        is ExpenseValidation.Valid -> {
                            saving = true
                            onSave(outcome.input) { result ->
                                saving = false
                                if (result == ExpenseSaveResult.SUCCESS) onDismiss()
                                else generalError = result.messageRes()
                            }
                        }
                    }
                }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(enabled = !saving, onClick = { confirmDelete = true }) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(enabled = !saving, onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        }
    )

    if (pickingDate) {
        ExpenseDatePickerDialog(
            initial = date,
            onDismiss = { pickingDate = false },
            onPick = { date = it; pickingDate = false }
        )
    }
    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.expense_delete_title)) },
            text = { Text(stringResource(R.string.expense_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    saving = true
                    onDelete { onDismiss() }
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDatePickerDialog(
    initial: LocalDate,
    onDismiss: () -> Unit,
    onPick: (LocalDate) -> Unit
) {
    // Material date pickers work in UTC midnight; convert without timezone drift.
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )
    val selected = state.selectedDateMillis
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = selected != null,
                onClick = {
                    if (selected != null) onPick(Instant.ofEpochMilli(selected).atZone(ZoneOffset.UTC).toLocalDate())
                }
            ) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    ) {
        DatePicker(state = state)
    }
}
