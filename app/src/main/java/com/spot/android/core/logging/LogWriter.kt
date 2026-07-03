package com.spot.android.core.logging

import android.util.Log
import javax.inject.Inject

interface LogWriter {
    fun write(
        priority: Int,
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )
}

class AndroidLogWriter @Inject constructor() : LogWriter {
    override fun write(
        priority: Int,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (throwable == null) {
            Log.println(priority, tag, message)
        } else {
            Log.println(priority, tag, "$message\n${Log.getStackTraceString(throwable)}")
        }
    }
}
