package com.spot.android.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.spot.android.MainActivity
import com.spot.android.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local notifications for social events (PRD/14).
 * Channels: FOLLOW_REQUEST, FOLLOW_ACCEPTED.
 */
@Singleton
class SpotNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        listOf(
            NotificationChannel(
                CHANNEL_FOLLOW_REQUEST,
                "Follow requests",
                NotificationManager.IMPORTANCE_HIGH,
            ),
            NotificationChannel(
                CHANNEL_FOLLOW_ACCEPTED,
                "Follow accepted",
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        ).forEach(manager::createNotificationChannel)
    }

    /**
     * Posts a local notification when the current user accepts someone's follow request
     * (mirrors iOS client-side FOLLOW_ACCEPTED delivery path for the acceptor's UX testing;
     * real inbound notify requires FCM — future).
     *
     * For v1 parity with the local "accepted" path: when User B accepts User A's request,
     * we notify for routing demos. Prefer calling [notifyFollowAccepted] with the acceptor username
     * when the *requester* would be notified — that needs push. This method posts locally so
     * the accept action still exercises the channel + tap routing.
     */
    fun notifyFollowAccepted(acceptorUsername: String, acceptorUid: String) {
        ensureChannels()
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_FOLLOW_ACCEPTED)
            putExtra(EXTRA_USER_ID, acceptorUid)
            putExtra(EXTRA_USERNAME, acceptorUsername)
        }
        val pending = PendingIntent.getActivity(
            context,
            acceptorUid.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_FOLLOW_ACCEPTED)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Follow Request Accepted")
            .setContentText("$acceptorUsername accepted your follow request.")
            .setAutoCancel(true)
            .setContentIntent(pending)
            .addAction(
                0,
                "View Profile",
                pending,
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIF_ID_FOLLOW_ACCEPTED_BASE + acceptorUid.hashCode().and(0xFFFF),
                notification,
            )
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted
        }
    }

    companion object {
        const val CHANNEL_FOLLOW_REQUEST = "FOLLOW_REQUEST"
        const val CHANNEL_FOLLOW_ACCEPTED = "FOLLOW_ACCEPTED"
        const val EXTRA_NOTIFICATION_TYPE = "spot_notification_type"
        const val EXTRA_USER_ID = "spot_notification_user_id"
        const val EXTRA_USERNAME = "spot_notification_username"
        const val TYPE_FOLLOW_ACCEPTED = "follow_accepted"
        const val TYPE_FOLLOW_REQUEST = "follow_request"
        private const val NOTIF_ID_FOLLOW_ACCEPTED_BASE = 4100
    }
}
