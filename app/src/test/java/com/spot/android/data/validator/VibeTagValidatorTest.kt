package com.spot.android.data.validator

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for VibeTagValidator.
 */
class VibeTagValidatorTest {
    
    @Test
    fun `valid vibe tags pass validation`() {
        val validVibes = listOf(
            "Chill Spot",
            "Hidden Gem",
            "Great For Photos",
            "ab", // min length
            "a".repeat(30) // max length
        )
        
        validVibes.forEach { vibe ->
            assertTrue("'$vibe' should be valid", VibeTagValidator.isValid(vibe))
            assertNull("'$vibe' should have no error", VibeTagValidator.validate(vibe))
        }
    }
    
    @Test
    fun `too short vibe tag fails`() {
        val vibe = "a"
        assertFalse(VibeTagValidator.isValid(vibe))
        assertNotNull(VibeTagValidator.validate(vibe))
        assertTrue(VibeTagValidator.validate(vibe)!!.contains("at least 2"))
    }
    
    @Test
    fun `too long vibe tag fails`() {
        val vibe = "a".repeat(31)
        assertFalse(VibeTagValidator.isValid(vibe))
        assertNotNull(VibeTagValidator.validate(vibe))
        assertTrue(VibeTagValidator.validate(vibe)!!.contains("at most 30"))
    }
    
    @Test
    fun `blank vibe tag fails`() {
        val vibes = listOf("", "   ", "\t", "\n")
        
        vibes.forEach { vibe ->
            assertFalse("'$vibe' should be invalid", VibeTagValidator.isValid(vibe))
            assertNotNull(VibeTagValidator.validate(vibe))
        }
    }
    
    @Test
    fun `vibe tag with leading trailing whitespace is trimmed`() {
        val vibe = "  Chill Spot  "
        assertTrue(VibeTagValidator.isValid(vibe))
    }
    
    @Test
    fun `normalize converts to lowercase`() {
        assertEquals("chill spot", VibeTagValidator.normalize("Chill Spot"))
        assertEquals("hidden gem", VibeTagValidator.normalize("Hidden Gem"))
        assertEquals("test", VibeTagValidator.normalize("TEST"))
    }
    
    @Test
    fun `normalize trims whitespace`() {
        assertEquals("chill spot", VibeTagValidator.normalize("  Chill Spot  "))
    }
}
