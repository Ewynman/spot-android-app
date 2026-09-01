package com.spot.android.core.util

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android proxy for iOS's `UIAccessibility.isReduceMotionEnabled`.
 *
 * There is no first-class "Reduce Motion" toggle on Android, but users can
 * disable animations globally via **Developer options → Animator duration
 * scale** (also honored by TalkBack / Accessibility). We treat a zero animator
 * duration scale as the equivalent signal.
 */
fun isReduceMotionEnabled(context: Context): Boolean = try {
    val scale = Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    )
    scale == 0f
} catch (_: Settings.SettingNotFoundException) {
    false
}

@Composable
fun rememberReduceMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) { isReduceMotionEnabled(context) }
}
