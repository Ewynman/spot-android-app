package com.spot.android.data.permissions

/**
 * Runtime permission state mirroring iOS authorization status.
 *
 * Reference: PRD/05-auth-onboarding.md
 */
enum class PermissionState {
    /** Permission has never been requested. */
    NOT_DETERMINED,

    /** Permission is granted or not required on this API level. */
    AUTHORIZED,

    /** Permission was denied but the OS dialog can still be shown. */
    DENIED,

    /** Permission was permanently denied; user must open app settings. */
    PERMANENTLY_DENIED,

    /** No runtime permission is required on this device API level. */
    NOT_REQUIRED,
}
