package com.burhannexus.dukaandost.ui.scanner

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.burhannexus.dukaandost.R

/**
 * Full-screen camera for scanning barcodes.
 *
 * [feedbackKey] changes with every result the caller wants to announce; each change plays a beep
 * (a different one for errors) and shows [feedbackText] at the bottom. Pass null/0 when nothing is announced.
 */
@Composable
fun BarcodeScanDialog(
    title: String,
    hint: String,
    feedbackKey: Long,
    feedbackText: String?,
    feedbackIsError: Boolean,
    onBarcode: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val tone = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: RuntimeException) {
            null
        }
    }
    DisposableEffect(Unit) { onDispose { tone?.release() } }
    LaunchedEffect(feedbackKey) {
        if (feedbackKey != 0L && feedbackText != null) {
            tone?.startTone(if (feedbackIsError) ToneGenerator.TONE_PROP_NACK else ToneGenerator.TONE_PROP_BEEP, 150)
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            Box(Modifier.fillMaxSize()) {
                BarcodeCamera(onBarcode = onBarcode, modifier = Modifier.fillMaxSize())

                // Aiming frame
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.8f)
                        .height(150.dp)
                        .border(BorderStroke(2.dp, Color.White), RoundedCornerShape(12.dp))
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text(hint, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (feedbackText != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (feedbackIsError) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                feedbackText,
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (feedbackIsError) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.scan_done))
                        }
                    }
                }
            }
        }
    }
}
