package com.burhannexus.dukaandost.domain.calc

import java.math.BigInteger
import java.util.Locale

/** Result of a code conversion: either [value] or an [error]. */
sealed interface CodeResult {
    data class Ok(val value: String) : CodeResult
    data class Error(val kind: CodeError, val detail: String = "") : CodeResult
}

enum class CodeError { EMPTY, BAD_LETTER, BAD_DIGIT }

/**
 * Price <-> letter code. A mapping is 10 distinct letters A-Z; the letter at position d stands for digit d.
 */
object SecretCode {
    const val DEFAULT_MAPPING = "AEIOUSBLWP"
    const val MAX_INPUT_LENGTH = 20

    fun isValidMapping(mapping: String?): Boolean =
        mapping != null && mapping.length == 10 && mapping.all { it in 'A'..'Z' } && mapping.toSet().size == 10

    fun validMappingOrDefault(mapping: String?): String =
        if (isValidMapping(mapping)) mapping!! else DEFAULT_MAPPING

    /** Letters -> price with thousands separators, e.g. "EAA" -> "100". */
    fun decode(raw: String, mapping: String): CodeResult {
        val code = raw.trim().uppercase(Locale.ROOT)
        if (code.isEmpty()) return CodeResult.Error(CodeError.EMPTY)
        val digits = StringBuilder()
        for (letter in code) {
            val index = mapping.indexOf(letter)
            if (index < 0) return CodeResult.Error(CodeError.BAD_LETTER, letter.toString())
            digits.append('0' + index)
        }
        return CodeResult.Ok(String.format(Locale.US, "%,d", BigInteger(digits.toString())))
    }

    /** Digits -> letters. */
    fun generate(raw: String, mapping: String): CodeResult {
        val digits = raw.trim()
        if (digits.isEmpty()) return CodeResult.Error(CodeError.EMPTY)
        val out = StringBuilder()
        for (ch in digits) {
            if (ch !in '0'..'9') return CodeResult.Error(CodeError.BAD_DIGIT, ch.toString())
            out.append(mapping[ch - '0'])
        }
        return CodeResult.Ok(out.toString())
    }
}
