package ca.sheridancollege.medreminder.worker

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import ca.sheridancollege.medreminder.MainActivity
import ca.sheridancollege.medreminder.R
import java.util.Calendar

object MedicationAlarmScheduler {

    const val CHANNEL_ID      = "med_reminders_v4"
    const val KEY_MED_ID      = "med_id"
    const val KEY_MED_NAME    = "med_name"
    const val KEY_MED_DOSAGE  = "med_dosage"
    const val KEY_HOUR        = "hour"
    const val KEY_MINUTE      = "minute"

    fun schedule(
        context: Context,
        medicationId: Int,
        medicationName: String,
        dosage: String,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(context, MedicationAlarmReceiver::class.java).also {
            it.putExtra(KEY_MED_ID,    medicationId)
            it.putExtra(KEY_MED_NAME,  medicationName)
            it.putExtra(KEY_MED_DOSAGE, dosage)
            it.putExtra(KEY_HOUR,      hour)
            it.putExtra(KEY_MINUTE,    minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = nextOccurrence(hour, minute)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancel(context: Context, medicationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun nextOccurrence(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) target.add(Calendar.DAY_OF_MONTH, 1)
        return target.timeInMillis
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            listOf("medication_reminders", "medication_reminders_v2", "medication_reminders_v3")
                .forEach { manager.deleteNotificationChannel(it) }
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return

            val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.med_reminder}")
            val audioAttr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(CHANNEL_ID, "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders to take your medication on time"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 600)
                setSound(soundUri, audioAttr)
                enableLights(true)
                lightColor = android.graphics.Color.CYAN
                setBypassDnd(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, medId: Int, medName: String, dosage: String) {
        createNotificationChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.med_reminder}")

        val openIntent = PendingIntent.getActivity(
            context, medId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("medicationId", medId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takenAction = run {
            val i = Intent(context, NotificationActionReceiver::class.java)
            i.action = NotificationActionReceiver.ACTION_MARK_TAKEN
            i.putExtra(KEY_MED_ID, medId)
            i.putExtra(KEY_MED_NAME, medName)
            PendingIntent.getBroadcast(
                context, medId + 10000, i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val snoozeAction = run {
            val i = Intent(context, NotificationActionReceiver::class.java)
            i.action = NotificationActionReceiver.ACTION_SNOOZE
            i.putExtra(KEY_MED_ID, medId)
            i.putExtra(KEY_MED_NAME, medName)
            i.putExtra(KEY_MED_DOSAGE, dosage)
            PendingIntent.getBroadcast(
                context, medId + 20000, i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val dosageText = if (dosage.isNotBlank()) " · $dosage" else ""

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💊 Time for $medName")
            .setContentText("Take your $medName$dosageText now")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's time to take your $medName$dosageText.\n\nTap 'Mark Taken' to confirm or snooze for 10 minutes.")
                .setBigContentTitle("💊 Medication Reminder"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 600))
            .setColor(0xFF00BCD4.toInt())
            .setColorized(true)
            .addAction(android.R.drawable.checkbox_on_background, "✓ Mark Taken", takenAction)
            .addAction(android.R.drawable.ic_menu_recent_history, "⏰ Snooze 10min", snoozeAction)
            .setFullScreenIntent(openIntent, true)
            .build()

        manager.notify(medId, notification)
    }
}
