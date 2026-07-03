package com.spot.android.data.model.enums

/**
 * Account status values from `users.account_status`.
 * 
 * Reference: PRD/03-data-model.md
 */
enum class AccountStatus(val value: String) {
    ACTIVE("active"),
    RESTRICTED("restricted"),
    SUSPENDED("suspended"),
    BANNED("banned");
    
    companion object {
        fun fromValue(value: String?): AccountStatus? {
            return entries.find { it.value == value }
        }
    }
}
