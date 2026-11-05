package com.burhannexus.dukaandost.ui.parties

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burhannexus.dukaandost.R
import com.burhannexus.dukaandost.data.sms.SmsResult
import com.burhannexus.dukaandost.domain.calc.ReminderMessage
import java.math.BigDecimal

/** True when a reminder makes sense: the customer owes money and has a phone number. */
fun canRemind(phone: String?, balance: BigDecimal): Boolean = !phone.isNullOrBlank() && balance.signum() > 0

/**
 * Shows the reminder text with the real amount filled in. One tap on Send (after this confirmation) sends it
 * directly as an SMS. The text itself can be edited and is remembered for next time.
 */
@Composable
fun ReminderDialog(
    name: String,
    phone: String,
    balance: BigDecimal,
    onDismiss: () -> Unit,
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val template by viewModel.template.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }

    val message = ReminderMessage.render(template, balance)
    val sentText = stringResource(R.string.reminder_sent, name)
    val failedText = stringResource(R.string.reminder_failed)
    val deniedText = stringResource(R.string.reminder_permission_denied)

    fun report(result: SmsResult) {
        when (result) {
            SmsResult.HANDED_OFF -> {
                Toast.makeText(context, sentText, Toast.LENGTH_LONG).show()
                onDismiss()
            }
            SmsResult.NO_PERMISSION -> Toast.makeText(context, deniedText, Toast.LENGTH_LONG).show()
            SmsResult.FAILED -> Toast.makeText(context, failedText, Toast.LENGTH_LONG).show()
        }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) report(viewModel.send(phone, message)) else report(SmsResult.NO_PERMISSION)
    }

    val tokenMissing = editing && !ReminderMessage.hasToken(draft)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reminder_title, name)) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.reminder_to, phone), style = MaterialTheme.typography.bodySmall)
                if (editing) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it.take(ReminderMessage.MAX_LENGTH) },
                        label = { Text(stringResource(R.string.reminder_template)) },
                        supportingText = {
                            Text(
                                stringResource(
                                    if (tokenMissing) R.string.reminder_token_missing else R.string.reminder_token_hint,
                                    ReminderMessage.AMOUNT_TOKEN
                                )
                            )
                        },
                        isError = tokenMissing,
                        minLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = { draft = ReminderMessage.DEFAULT_TEMPLATE }) {
                        Text(stringResource(R.string.reminder_reset))
                    }
                } else {
                    Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        },
        confirmButton = {
            if (editing) {
                TextButton(
                    enabled = draft.isNotBlank() && !tokenMissing,
                    onClick = {
                        viewModel.saveTemplate(draft)
                        editing = false
                    }
                ) { Text(stringResource(R.string.save)) }
            } else {
                TextButton(onClick = {
                    if (viewModel.hasPermission()) {
                        report(viewModel.send(phone, message))
                    } else {
                        permission.launch(Manifest.permission.SEND_SMS)
                    }
                }) { Text(stringResource(R.string.reminder_send)) }
            }
        },
        dismissButton = {
            if (editing) {
                TextButton(onClick = { editing = false }) { Text(stringResource(R.string.cancel)) }
            } else {
                Column {
                    TextButton(onClick = { draft = template; editing = true }) {
                        Text(stringResource(R.string.reminder_edit))
                    }
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                }
            }
        }
    )
}
