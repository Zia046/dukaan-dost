package com.burhannexus.dukaandost.core

enum class BarcodeSymbology { EAN13, EAN8, UPCA, CODE128 }

/** Barcode rules shared by scanning and printing. Pure functions, no Android classes. */
object Barcodes {
    /** GS1 check digit for the digits before it (EAN-13: 12 digits, EAN-8: 7 digits, UPC-A: 11 digits). */
    fun checkDigit(body: String): Int {
        require(body.isNotEmpty() && body.all { it in '0'..'9' }) { "Digits only" }
        val sum = body.reversed().withIndex().sumOf { (i, c) -> (c - '0') * (if (i % 2 == 0) 3 else 1) }
        return (10 - sum % 10) % 10
    }

    private fun hasValidCheckDigit(code: String, length: Int): Boolean =
        code.length == length && code.all { it in '0'..'9' } && checkDigit(code.dropLast(1)) == code.last() - '0'

    fun isValidEan13(code: String) = hasValidCheckDigit(code, 13)
    fun isValidEan8(code: String) = hasValidCheckDigit(code, 8)
    fun isValidUpcA(code: String) = hasValidCheckDigit(code, 12)

    /** The right symbology for printing [code]. Anything that is not a valid retail code becomes Code 128. */
    fun symbologyFor(code: String): BarcodeSymbology = when {
        isValidEan13(code) -> BarcodeSymbology.EAN13
        isValidEan8(code) -> BarcodeSymbology.EAN8
        isValidUpcA(code) -> BarcodeSymbology.UPCA
        else -> BarcodeSymbology.CODE128
    }

    /** Code 128 can print plain keyboard characters (space to ~) up to a sensible length. */
    fun isPrintable(code: String): Boolean = code.isNotEmpty() && code.length <= 40 && code.all { it in ' '..'~' }

    /**
     * Shop-internal EAN-13. GS1 reserves prefixes 20-29 for in-store use, so these numbers can never clash
     * with a manufacturer's barcode. [serial] is made unique by the caller (the product id).
     */
    fun internalEan13(serial: Long): String {
        require(serial in 0..99_999_999_999L) { "Serial out of range" }
        val body = "2" + serial.toString().padStart(11, '0')
        return body + checkDigit(body)
    }

    /**
     * A phone camera may report a UPC-A as 13 digits starting with 0 (or the reverse), so a product
     * saved either way must still be found.
     */
    fun lookupCandidates(scanned: String): List<String> {
        val code = scanned.trim()
        if (code.isEmpty()) return emptyList()
        val extra = when {
            code.length == 13 && code.startsWith("0") && code.all { it.isDigit() } -> code.drop(1)
            code.length == 12 && code.all { it.isDigit() } -> "0$code"
            else -> null
        }
        return listOfNotNull(code, extra)
    }
}
