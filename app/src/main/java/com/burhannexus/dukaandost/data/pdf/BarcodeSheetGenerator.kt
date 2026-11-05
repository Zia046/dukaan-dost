package com.burhannexus.dukaandost.data.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.burhannexus.dukaandost.core.BarcodeSymbology
import com.burhannexus.dukaandost.core.Barcodes
import com.burhannexus.dukaandost.core.LabelSheet
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * Draws an A4 PDF of small barcode labels: 65 per page (5 x 13). Each label has the product name, the
 * barcode as sharp vector bars and the number under it. EAN-13, EAN-8 and UPC-A numbers are printed in
 * their own symbology; anything else (for example a phone IMEI) is printed as Code 128.
 */
@Singleton
class BarcodeSheetGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val black = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.FILL }
    private val cutPaint = Paint().apply {
        color = Color.rgb(176, 176, 176)
        style = Paint.Style.STROKE
        strokeWidth = 0.4f
    }
    private val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.DEFAULT_BOLD
        textSize = NAME_SIZE
        textAlign = Paint.Align.CENTER
    }
    private val digitsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
    }

    /**
     * @throws IllegalArgumentException when [code] cannot be drawn as a barcode.
     * @return the PDF in the app cache, ready to share or print.
     */
    suspend fun generate(productName: String, code: String, copies: Int, cutLines: Boolean): File =
        withContext(Dispatchers.Default) {
            require(copies in 1..LabelSheet.MAX_COPIES) { "Copies out of range" }
            require(Barcodes.isPrintable(code)) { "This barcode cannot be printed" }
            val symbology = Barcodes.symbologyFor(code)
            val matrix = encode(code, symbology)

            val directory = File(context.cacheDir, "barcodes").apply { mkdirs() }
            directory.listFiles()?.forEach { it.delete() }
            val file = File(directory, "barcodes.pdf")

            val document = PdfDocument()
            try {
                for (page in 0 until LabelSheet.pageCount(copies)) {
                    val info = PdfDocument.PageInfo.Builder(LabelSheet.PAGE_WIDTH_PT, LabelSheet.PAGE_HEIGHT_PT, page + 1).create()
                    val pdfPage = document.startPage(info)
                    val onPage = LabelSheet.labelsOnPage(copies, page)
                    for (index in 0 until onPage) {
                        drawLabel(
                            canvas = pdfPage.canvas,
                            left = LabelSheet.mmToPt(LabelSheet.LEFT_MARGIN_MM + (index % LabelSheet.COLUMNS) * LabelSheet.COLUMN_PITCH_MM),
                            top = LabelSheet.mmToPt(LabelSheet.TOP_MARGIN_MM + (index / LabelSheet.COLUMNS) * LabelSheet.ROW_PITCH_MM),
                            name = productName,
                            code = code,
                            matrix = matrix,
                            quietModules = if (symbology == BarcodeSymbology.CODE128) 10 else 7,
                            cutLines = cutLines
                        )
                    }
                    document.finishPage(pdfPage)
                }
                file.outputStream().use { document.writeTo(it) }
            } finally {
                document.close()
            }
            file
        }

    private fun encode(code: String, symbology: BarcodeSymbology): BitMatrix {
        val format = when (symbology) {
            BarcodeSymbology.EAN13 -> BarcodeFormat.EAN_13
            BarcodeSymbology.EAN8 -> BarcodeFormat.EAN_8
            BarcodeSymbology.UPCA -> BarcodeFormat.UPC_A
            BarcodeSymbology.CODE128 -> BarcodeFormat.CODE_128
        }
        // Width/height 1 and margin 0 return exactly one module per bar unit; the quiet zone is added when drawing.
        return MultiFormatWriter().encode(code, format, 1, 1, mapOf(EncodeHintType.MARGIN to 0))
    }

    private fun drawLabel(
        canvas: Canvas,
        left: Float,
        top: Float,
        name: String,
        code: String,
        matrix: BitMatrix,
        quietModules: Int,
        cutLines: Boolean
    ) {
        val width = LabelSheet.mmToPt(LabelSheet.LABEL_WIDTH_MM)
        val height = LabelSheet.mmToPt(LabelSheet.LABEL_HEIGHT_MM)
        val centerX = left + width / 2f
        if (cutLines) canvas.drawRect(left, top, left + width, top + height, cutPaint)

        val padding = LabelSheet.mmToPt(1.0)
        val modules = matrix.width
        // Nominal module width is 0.33 mm (100% magnification); narrower only when the code is long.
        val module = min(LabelSheet.mmToPt(0.33), (width - 2 * padding) / (modules + 2 * quietModules))
        val barsWidth = modules * module
        val barsLeft = left + (width - barsWidth) / 2f
        val barsTop = top + BARS_TOP
        val barsBottom = barsTop + BARS_HEIGHT

        var x = 0
        while (x < modules) {
            if (matrix.get(x, 0)) {
                var end = x
                while (end < modules && matrix.get(end, 0)) end++
                canvas.drawRect(barsLeft + x * module, barsTop, barsLeft + end * module, barsBottom, black)
                x = end
            } else {
                x++
            }
        }

        canvas.drawText(fit(name, namePaint, width - 2 * padding), centerX, top + NAME_BASELINE, namePaint)

        digitsPaint.textSize = DIGITS_SIZE
        while (digitsPaint.measureText(code) > width - 2 * padding && digitsPaint.textSize > MIN_DIGITS_SIZE) {
            digitsPaint.textSize -= 0.25f
        }
        canvas.drawText(code, centerX, barsBottom + DIGITS_GAP + digitsPaint.textSize, digitsPaint)
    }

    private fun fit(text: String, paint: Paint, maxWidth: Float): String {
        val clean = text.trim()
        if (paint.measureText(clean) <= maxWidth) return clean
        val room = maxWidth - paint.measureText(ELLIPSIS)
        val count = paint.breakText(clean, true, room, null)
        return clean.take(count).trimEnd() + ELLIPSIS
    }

    private companion object {
        const val NAME_SIZE = 6.5f
        const val DIGITS_SIZE = 7f
        const val MIN_DIGITS_SIZE = 4.5f
        const val NAME_BASELINE = 8.5f
        const val BARS_TOP = 12f
        const val BARS_HEIGHT = 30f
        const val DIGITS_GAP = 2f
        const val ELLIPSIS = "…"
    }
}
