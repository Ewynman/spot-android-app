package com.spot.android.data.validator

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for UsernameValidator.
 */
class UsernameValidatorTest {
    
    @Test
    fun `valid usernames pass validation`() {
        val validUsernames = listOf(
            "alice",
            "bob_123",
            "user.name",
            "test_user_1",
            "a_b_c",
            "user.test.123"
        )
        
        validUsernames.forEach { username ->
            assertTrue("'$username' should be valid", UsernameValidator.isValid(username))
            assertNull("'$username' should have no error", UsernameValidator.validate(username))
        }
    }
    
    @Test
    fun `too short username fails`() {
        val username = "ab"
        assertFalse(UsernameValidator.isValid(username))
        assertNotNull(UsernameValidator.validate(username))
        assertTrue(UsernameValidator.validate(username)!!.contains("at least"))
    }
    
    @Test
    fun `too long username fails`() {
        val username = "a".repeat(21)
        assertFalse(UsernameValidator.isValid(username))
        assertNotNull(UsernameValidator.validate(username))
        assertTrue(UsernameValidator.validate(username)!!.contains("at most"))
    }
    
    @Test
    fun `username with invalid characters fails`() {
        val invalidUsernames = listOf(
            "user@name",
            "user name",
            "user-name",
            "user#123",
            "user!",
            "user%test"
        )
        
        invalidUsernames.forEach { username ->
            assertFalse("'$username' should be invalid", UsernameValidator.isValid(username))
            val error = UsernameValidator.validate(username)
            assertNotNull(error)
            assertTrue(error!!.contains("only contain"))
        }
    }
    
    @Test
    fun `username starting with period or underscore fails`() {
        val invalidUsernames = listOf(
            ".username",
            "_username"
        )
        
        invalidUsernames.forEach { username ->
            assertFalse("'$username' should be invalid", UsernameValidator.isValid(username))
            val error = UsernameValidator.validate(username)
            assertNotNull(error)
            assertTrue(error!!.contains("cannot start"))
        }
    }
    
    @Test
    fun `username ending with period or underscore fails`() {
        val invalidUsernames = listOf(
            "username.",
            "username_"
        )
        
        invalidUsernames.forEach { username ->
            assertFalse("'$username' should be invalid", UsernameValidator.isValid(username))
            val error = UsernameValidator.validate(username)
            assertNotNull(error)
            assertTrue(error!!.contains("cannot start"))
        }
    }
    
    @Test
    fun `username with consecutive periods or underscores fails`() {
        val invalidUsernames = listOf(
            "user..name",
            "user__name",
            "user...test"
        )
        
        invalidUsernames.forEach { username ->
            assertFalse("'$username' should be invalid", UsernameValidator.isValid(username))
            val error = UsernameValidator.validate(username)
            assertNotNull(error)
            assertTrue(error!!.contains("consecutive"))
        }
    }
    
    @Test
    fun `minimum valid length username passes`() {
        val username = "abc"
        assertTrue(UsernameValidator.isValid(username))
    }
    
    @Test
    fun `maximum valid length username passes`() {
        val username = "a".repeat(20)
        assertTrue(UsernameValidator.isValid(username))
    }
}
