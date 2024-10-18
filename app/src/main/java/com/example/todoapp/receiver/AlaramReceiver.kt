package com.example.todoapp.receiver

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.todoapp.MainActivity
import com.example.todoapp.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // Ensure the context is not null
        context ?: return

        // Get notification details from the intent
        val notificationId = intent?.getIntExtra("notificationId", 0) ?: 0
        val message = intent?.getStringExtra("message") ?: "Task Reminder"

        // Create an intent to launch MainActivity
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context)
        }

        // Check if the app has the required notification permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            // Build and show the notification
            val notificationBuilder = NotificationCompat.Builder(context, "RoutineCraft")
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Aventus Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, notificationBuilder.build())
        } else {
            // Permission was not granted, handle it accordingly
            Toast.makeText(context, "Notification permission not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    // Create the notification channel
    private fun createNotificationChannel(context: Context) {
        val channelId = "RoutineCraft"
        val channelName = "Aventus Notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val notificationChannel = NotificationChannel(channelId, channelName, importance)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(notificationChannel)
    }
}
