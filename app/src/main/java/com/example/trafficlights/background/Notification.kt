package com.example.trafficlights.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.trafficlights.CHANNEL_ID
import com.example.trafficlights.CHANNEL_NAME
import com.example.trafficlights.DEBUG_TAG

import com.example.trafficlights.activities.MainActivity
import com.example.trafficlights.notificationId


class Notification(private val context: Context, private val token: String, private val status: String) {


    fun createNotification() {
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
                .setContentTitle("Прогресс по вашей заявке!")
                .setContentText("Статус одной из ваших заявок изменился")
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText("Ваше заявка $token была переведена в статус: $status"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        //show notification
        Log.d(DEBUG_TAG, "Создано уведоомление")
        notificationManager.notify(notificationId, builder.build())
    }
}