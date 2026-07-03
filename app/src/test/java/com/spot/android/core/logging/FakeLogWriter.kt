package com.spot.android.core.logging

class FakeLogWriter : LogWriter {
    data class Entry(
        val priority: Int,
        val tag: String,
        val message: String,
        val throwable: Throwable?,
    )

    val entries = mutableListOf<Entry>()

    override fun write(
        priority: Int,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        entries.add(Entry(priority, tag, message, throwable))
    }
}
