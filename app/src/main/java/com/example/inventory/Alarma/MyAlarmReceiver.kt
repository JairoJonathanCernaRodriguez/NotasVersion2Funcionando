package com.example.inventory.Alarma

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.inventory.MainActivity
import com.example.inventory.R
import com.example.inventory.ui.item.NotificacionApp


class MyAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_ID = 5
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val title = intent?.getStringExtra("title") ?: "Recordatorio"
        val message = intent?.getStringExtra("message") ?: "Tienes tareas pendientes."

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, NotificacionApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Cambia este icono por uno válido
            .setContentTitle(title) // Usa el título de la nota
            .setContentText(message) // Usa el contenido de la nota
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Alta prioridad para que "salte"
            .setAutoCancel(true) // Desaparece al tocar
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
