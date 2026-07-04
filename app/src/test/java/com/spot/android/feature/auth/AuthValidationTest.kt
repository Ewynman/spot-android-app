package com.spot.android.feature.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    @Test
    fun validEmail_passes() {
        assertTrue(AuthValidation.isValidEmail("user@example.com"))
    }

    @Test
    fun invalidEmail_fails() {
        assertFalse(AuthValidation.isValidEmail("not-an-email"))
    }

    @Test
    fun validUsername_passes() {
        assertTrue(AuthValidation.isValidUsername("spot_user123"))
    }

    @Test
    fun invalidUsername_withSpaces_fails() {
        assertFalse(AuthValidation.isValidUsername("bad user"))
    }

    @Test
    fun passwordMatch_checksEquality() {
        assertTrue(AuthValidation.passwordsMatch("password123", "password123"))
        assertFalse(AuthValidation.passwordsMatch("password123", "different"))
    }
}

class AuthErrorMessagesTest {

    @Test
    fun maskEmail_hidesMiddleOfLocalPart() {
        assertEquals("us****@example.com", maskEmail("user@example.com"))
    }

    @Test
    fun maskEmail_handlesShortLocalPart() {
        assertEquals("a****@example.com", maskEmail("a@example.com"))
    }
}
