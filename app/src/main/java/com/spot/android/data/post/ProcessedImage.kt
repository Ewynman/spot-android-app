package com.spot.android.data.post

/**
 * Downscaled JPEG ready for upload.
 */
data class ProcessedImage(
    val fileName: String,
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
) {
    val byteSize: Int get() = bytes.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProcessedImage) return false
        return fileName == other.fileName &&
            bytes.contentEquals(other.bytes) &&
            width == other.width &&
            height == other.height
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }
}
