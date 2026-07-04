package com.spot.android.data.terms

class FakeTermsRepository : TermsRepository {

    var hasAcceptedResult: Result<Boolean> = Result.success(true)
    var checkCalls = 0

    override suspend fun hasAcceptedActiveTerms(): Result<Boolean> {
        checkCalls++
        return hasAcceptedResult
    }
}
