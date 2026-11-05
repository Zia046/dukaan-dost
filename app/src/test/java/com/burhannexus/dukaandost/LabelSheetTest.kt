package com.burhannexus.dukaandost

import com.burhannexus.dukaandost.core.LabelSheet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LabelSheetTest {
    @Test fun sixtyFiveLabelsPerPage() = assertEquals(65, LabelSheet.PER_PAGE)

    @Test fun pageCountRoundsUp() {
        assertEquals(0, LabelSheet.pageCount(0))
        assertEquals(1, LabelSheet.pageCount(1))
        assertEquals(1, LabelSheet.pageCount(65))
        assertEquals(2, LabelSheet.pageCount(66))
        assertEquals(2, LabelSheet.pageCount(130))
        assertEquals(3, LabelSheet.pageCount(131))
        assertEquals(16, LabelSheet.pageCount(LabelSheet.MAX_COPIES))
    }

    @Test fun lastPageHoldsTheRemainder() {
        assertEquals(65, LabelSheet.labelsOnPage(70, 0))
        assertEquals(5, LabelSheet.labelsOnPage(70, 1))
        assertEquals(0, LabelSheet.labelsOnPage(70, 2))
        val total = (0 until LabelSheet.pageCount(1_000)).sumOf { LabelSheet.labelsOnPage(1_000, it) }
        assertEquals(1_000, total)
    }

    @Test fun everyLabelStaysInsideTheA4Page() {
        val right = LabelSheet.LEFT_MARGIN_MM + (LabelSheet.COLUMNS - 1) * LabelSheet.COLUMN_PITCH_MM + LabelSheet.LABEL_WIDTH_MM
        val bottom = LabelSheet.TOP_MARGIN_MM + (LabelSheet.ROWS - 1) * LabelSheet.ROW_PITCH_MM + LabelSheet.LABEL_HEIGHT_MM
        assertTrue(LabelSheet.mmToPt(right) < LabelSheet.PAGE_WIDTH_PT)
        assertTrue(LabelSheet.mmToPt(bottom) < LabelSheet.PAGE_HEIGHT_PT)
    }

    @Test fun labelsDoNotOverlapEachOther() {
        assertTrue(LabelSheet.COLUMN_PITCH_MM >= LabelSheet.LABEL_WIDTH_MM)
        assertTrue(LabelSheet.ROW_PITCH_MM >= LabelSheet.LABEL_HEIGHT_MM)
    }

    @Test fun millimetresConvertToPoints() {
        assertEquals(72f, LabelSheet.mmToPt(25.4), 0.001f)
    }
}
