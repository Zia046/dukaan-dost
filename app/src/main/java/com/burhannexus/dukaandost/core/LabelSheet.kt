package com.burhannexus.dukaandost.core

/**
 * A4 sheet of 65 small labels (5 across, 13 down, each 38.1 x 21.2 mm). This is the common
 * "65 per sheet" sticker layout, and it also cuts neatly from plain paper. Millimetres unless noted.
 */
object LabelSheet {
    const val COLUMNS = 5
    const val ROWS = 13
    const val PER_PAGE = COLUMNS * ROWS

    const val LABEL_WIDTH_MM = 38.1
    const val LABEL_HEIGHT_MM = 21.2
    const val COLUMN_PITCH_MM = 40.64
    const val ROW_PITCH_MM = 21.2
    const val LEFT_MARGIN_MM = 4.67
    const val TOP_MARGIN_MM = 10.7

    /** A4 in PDF points (1/72 inch). */
    const val PAGE_WIDTH_PT = 595
    const val PAGE_HEIGHT_PT = 842

    const val MAX_COPIES = 1_000

    fun mmToPt(mm: Double): Float = (mm * 72.0 / 25.4).toFloat()

    fun pageCount(copies: Int): Int = if (copies <= 0) 0 else (copies + PER_PAGE - 1) / PER_PAGE

    /** How many labels go on page [pageIndex] (0-based) when [copies] labels are printed in total. */
    fun labelsOnPage(copies: Int, pageIndex: Int): Int = (copies - pageIndex * PER_PAGE).coerceIn(0, PER_PAGE)
}
