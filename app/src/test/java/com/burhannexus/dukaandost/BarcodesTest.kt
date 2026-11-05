package com.burhannexus.dukaandost

import com.burhannexus.dukaandost.core.BarcodeSymbology
import com.burhannexus.dukaandost.core.Barcodes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BarcodesTest {
    @Test fun checkDigitsMatchKnownCodes() {
        assertEquals(1, Barcodes.checkDigit("400638133393")) // EAN-13 4006381333931
        assertEquals(2, Barcodes.checkDigit("03600029145")) // UPC-A 036000291452
        assertEquals(7, Barcodes.checkDigit("7351353")) // EAN-8 73513537
    }

    @Test fun validatesRetailCodes() {
        assertTrue(Barcodes.isValidEan13("4006381333931"))
        assertTrue(Barcodes.isValidUpcA("036000291452"))
        assertTrue(Barcodes.isValidEan8("73513537"))
    }

    @Test fun rejectsWrongCheckDigitLengthAndLetters() {
        assertFalse(Barcodes.isValidEan13("4006381333932"))
        assertFalse(Barcodes.isValidEan13("400638133393"))
        assertFalse(Barcodes.isValidEan13("40063813339X1"))
        assertFalse(Barcodes.isValidUpcA("036000291453"))
        assertFalse(Barcodes.isValidEan8("73513538"))
    }

    @Test fun picksTheRightSymbology() {
        assertEquals(BarcodeSymbology.EAN13, Barcodes.symbologyFor("4006381333931"))
        assertEquals(BarcodeSymbology.UPCA, Barcodes.symbologyFor("036000291452"))
        assertEquals(BarcodeSymbology.EAN8, Barcodes.symbologyFor("73513537"))
        assertEquals(BarcodeSymbology.CODE128, Barcodes.symbologyFor("ABC-123"))
        assertEquals(BarcodeSymbology.CODE128, Barcodes.symbologyFor("356938035643809")) // a 15-digit phone IMEI
        assertEquals(BarcodeSymbology.CODE128, Barcodes.symbologyFor("4006381333932")) // wrong check digit
    }

    @Test fun internalCodesAreValidEan13InTheStoreRange() {
        assertEquals("2000000000015", Barcodes.internalEan13(1))
        val code = Barcodes.internalEan13(123_456)
        assertEquals(13, code.length)
        assertTrue(code.startsWith("2"))
        assertTrue(Barcodes.isValidEan13(code))
        assertTrue(Barcodes.isValidEan13(Barcodes.internalEan13(99_999_999_999L)))
    }

    @Test fun internalCodesAreDifferentForDifferentProducts() {
        assertEquals(1000, (1L..1000L).map { Barcodes.internalEan13(it) }.toSet().size)
    }

    @Test fun internalCodeSerialMustBeInRange() {
        assertThrows(IllegalArgumentException::class.java) { Barcodes.internalEan13(-1) }
        assertThrows(IllegalArgumentException::class.java) { Barcodes.internalEan13(100_000_000_000L) }
    }

    @Test fun printableMeansKeyboardCharactersOfSensibleLength() {
        assertTrue(Barcodes.isPrintable("ABC123"))
        assertTrue(Barcodes.isPrintable("A B-1"))
        assertFalse(Barcodes.isPrintable(""))
        assertFalse(Barcodes.isPrintable("اردو"))
        assertFalse(Barcodes.isPrintable("1".repeat(41)))
    }

    @Test fun lookupAcceptsUpcAAsEan13AndBack() {
        assertEquals(listOf("4006381333931"), Barcodes.lookupCandidates(" 4006381333931 "))
        assertEquals(listOf("036000291452", "0036000291452"), Barcodes.lookupCandidates("036000291452"))
        assertEquals(listOf("0036000291452", "036000291452"), Barcodes.lookupCandidates("0036000291452"))
        assertEquals(listOf("ABC"), Barcodes.lookupCandidates("ABC"))
        assertTrue(Barcodes.lookupCandidates("   ").isEmpty())
    }
}
