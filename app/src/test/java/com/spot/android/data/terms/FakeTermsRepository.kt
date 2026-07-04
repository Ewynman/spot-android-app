package com.spot.android.data.terms

class FakeTermsRepository : TermsRepository {

    var hasAcceptedResult: Result<Boolean> = Result.success(true)
    var recordResult: Result<Unit> = Result.success(Unit)
    var checkCalls = 0
    var recordCalls = 0

    override suspend fun hasAcceptedActiveTerms(): Result<Boolean> {
        checkCalls++
        return hasAcceptedResult
    }

    override suspend fun recordTermsAcceptance(
        appVersion: String?,
        buildNumber: String?,
        deviceInfo: String?,
    ): Result<Unit> {
        recordCalls++
        return recordResult
    }
}
