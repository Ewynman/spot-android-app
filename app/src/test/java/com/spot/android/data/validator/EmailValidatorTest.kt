package com.spot.android.data.validator

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for EmailValidator.
 */
class EmailValidatorTest {
    
    @Test
    fun `valid emails pass validation`() {
        val validEmails = listOf(
            "user@example.com",
            "test.user@example.com",
            "user+tag@example.com",
            "user123@test-domain.com",
            "a@b.co"
        )
        
        validEmails.forEach { email ->
            assertTrue("'$email' should be valid", EmailValidator.isValid(email))
            assertNull("'$email' should have no error", EmailValidator.validate(email))
        }
    }
    
    @Test
    fun `invalid emails fail validation`() {
        val invalidEmails = listOf(
            "not-an-email",
            "@example.com",
            "user@",
            "user space@example.com",
            "user@example",
            ""
        )
        
        invalidEmails.forEach { email ->
            assertFalse("'$email' should be invalid", EmailValidator.isValid(email))
            assertNotNull("'$email' should have error", EmailValidator.validate(email))
        }
    }
    
    @Test
    fun `blank email fails`() {
        val emails = listOf("", "   ", "\t")
        
        emails.forEach { email ->
            assertFalse("'$email' should be invalid", EmailValidator.isValid(email))
            val error = EmailValidator.validate(email)
            assertNotNull(error)
            assertTrue(error!!.contains("cannot be empty"))
        }
    }
    
    @Test
    fun `normalize converts to lowercase`() {
        assertEquals("user@example.com", EmailValidator.normalize("User@Example.com"))
        assertEquals("test@test.com", EmailValidator.normalize("TEST@TEST.COM"))
    }
    
    @Test
    fun `normalize trims whitespace`() {
        assertEquals("user@example.com", EmailValidator.normalize("  user@example.com  "))
    }
}
