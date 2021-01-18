package com.example.trafficlights.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

import com.example.trafficlights.activities.MainActivity


const val CHANNEL_ID = "777"
class Notification(private val context: Context, private val token: String) {
    private val CHANNEL_NAME: CharSequence = "Takeit"
    private val notificationId = 1

    public fun createNotification() {
        var notificationChannel: NotificationChannel

        //intent to open our activity
        val intent = Intent(context.applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("Notify", true)
        val pendingIntent = PendingIntent.getActivity(context.applicationContext, 0, intent, 0)

        val notificationManager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        //notifications
        val builder = NotificationCompat.Builder(context.applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Ваша проблема была устранена")
                .setContentText("Ваше заявка $token была переведена в статус: Решено")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        //show notification

        notificationManager.notify(notificationId, builder.build())
    }
}