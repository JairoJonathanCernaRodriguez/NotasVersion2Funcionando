package com.example.inventory.Alarma


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.inventory.ui.item.AlarmReceiverNotificacion
import java.time.format.DateTimeFormatter


class AlarmSchedulerImpl(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java) as AlarmManager

    fun schedule(alarmItem: AlarmItem) {
        val intent = Intent(context, MyAlarmReceiver::class.java).apply {
            putExtra("title", "Revisa tus recordatorios pendientes...")
            putExtra("message", alarmItem.message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmItem.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmItem.tiempoMilis, // Hora especificada en `AlarmItem`
            pendingIntent
        )

        Log.d("AlarmSchedulerImpl", "Alarm set for: ${alarmItem.alarmTime}")
    }
}
