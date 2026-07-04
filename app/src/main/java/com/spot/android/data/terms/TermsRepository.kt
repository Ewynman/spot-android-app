package com.spot.android.data.terms

/**
 * Terms acceptance checks against the active terms version.
 *
 * Reference: PRD/04-backend-api.md, PRD/05-auth-onboarding.md
 */
interface TermsRepository {

    /**
     * Returns whether the authenticated caller has accepted the active terms version.
     */
    suspend fun hasAcceptedActiveTerms(): Result<Boolean>
}
