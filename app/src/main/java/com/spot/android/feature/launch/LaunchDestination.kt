package com.spot.android.feature.launch

/**
 * Root destinations resolved by the launch gate.
 *
 * Reference: PRD/05-auth-onboarding.md, PRD/00-overview.md
 */
enum class LaunchDestination {
    Splash,
    ConfirmEmail,
    Welcome,
    UsernameSetup,
    TermsUpdate,
    MainShell,
}
