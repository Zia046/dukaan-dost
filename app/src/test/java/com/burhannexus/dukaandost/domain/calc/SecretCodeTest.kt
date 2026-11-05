package com.burhannexus.dukaandost.domain.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretCodeTest {
    private val map = SecretCode.DEFAULT_MAPPING

    @Test fun generateMapsDigitsToLetters() {
        assertEquals(CodeResult.Ok("EAA"), SecretCode.generate("100", map))
    }

    @Test fun decodeAddsThousandsSeparators() {
        assertEquals(CodeResult.Ok("1,240"), SecretCode.decode("EIUA", map))
    }

    @Test fun roundTrip() {
        val code = (SecretCode.generate("9876543210", map) as CodeResult.Ok).value
        assertEquals(CodeResult.Ok("9,876,543,210"), SecretCode.decode(code, map))
    }

    @Test fun decodeIsCaseInsensitiveAndTrims() {
        assertEquals(CodeResult.Ok("100"), SecretCode.decode("  eaa ", map))
    }

    @Test fun errors() {
        assertEquals(CodeResult.Error(CodeError.EMPTY), SecretCode.decode("  ", map))
        assertEquals(CodeResult.Error(CodeError.BAD_LETTER, "Z"), SecretCode.decode("EZ", map))
        assertEquals(CodeResult.Error(CodeError.BAD_DIGIT, "x"), SecretCode.generate("1x", map))
    }

    @Test fun mappingValidation() {
        assertTrue(SecretCode.isValidMapping("AEIOUSBLWP"))
        assertFalse(SecretCode.isValidMapping("AAIOUSBLWP"))
        assertFalse(SecretCode.isValidMapping("AEIOUSBLW"))
        assertFalse(SecretCode.isValidMapping("aeiouSBLWP"))
        assertFalse(SecretCode.isValidMapping(null))
        assertEquals(map, SecretCode.validMappingOrDefault("bad"))
    }
}
