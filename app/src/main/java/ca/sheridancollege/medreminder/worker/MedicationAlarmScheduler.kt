package ca.sheridancollege.medreminder.worker

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import ca.sheridancollege.medreminder.MainActivity
import ca.sheridancollege.medreminder.R
import java.util.Calendar

object MedicationAlarmScheduler {

    const val CHANNEL_ID   = "med_reminders_v5"
    private const val CHANNEL_ID_LEGACY = "med_reminders_v4"
    const val KEY_MED_ID   = "med_id"
    const val KEY_MED_NAME = "med_name"
    const val KEY_DOSE_ID  = "dose_id"
    const val KEY_HOUR     = "hour"
    const val KEY_MINUTE   = "minute"

    fun createNotificationChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.deleteNotificationChannel(CHANNEL_ID_LEGACY)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val soundUri = "android.resource://${context.packageName}/${R.raw.med_reminder}".toUri()
        val audioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(CHANNEL_ID, "Medication Reminders",
            NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Reminders to take your scheduled medications"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 600)
            setSound(soundUri, audioAttr)
            enableLights(true)
            lightColor = android.graphics.Color.RED
            setBypassDnd(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun getRequestCode(doseEventId: Int): Int = doseEventId

    fun schedule(
        context: Context,
        doseEvent: ca.sheridancollege.medreminder.domain.model.DoseEvent
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra(KEY_MED_ID,   doseEvent.medicationId)
            putExtra(KEY_MED_NAME, doseEvent.medicationName)
            putExtra(KEY_DOSE_ID,  doseEvent.id)
            putExtra(KEY_HOUR,     Calendar.getInstance().apply { timeInMillis = doseEvent.scheduledTime }.get(Calendar.HOUR_OF_DAY))
            putExtra(KEY_MINUTE,   Calendar.getInstance().apply { timeInMillis = doseEvent.scheduledTime }.get(Calendar.MINUTE))
        }

        val requestCode = getRequestCode(doseEvent.id)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (doseEvent.scheduledTime <= System.currentTimeMillis()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(doseEvent.scheduledTime, pendingIntent),
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, doseEvent.scheduledTime, pendingIntent)
        }
    }

    fun cancel(context: Context, doseEventId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context, getRequestCode(doseEventId),
            Intent(context, MedicationAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun snooze(context: Context, doseEvent: ca.sheridancollege.medreminder.domain.model.DoseEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000L

        val alarmIntent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra(KEY_MED_ID,   doseEvent.medicationId)
            putExtra(KEY_MED_NAME, doseEvent.medicationName)
            putExtra(KEY_DOSE_ID,  doseEvent.id)
            putExtra(KEY_HOUR,     Calendar.getInstance().apply { timeInMillis = doseEvent.scheduledTime }.get(Calendar.HOUR_OF_DAY))
            putExtra(KEY_MINUTE,   Calendar.getInstance().apply { timeInMillis = doseEvent.scheduledTime }.get(Calendar.MINUTE))
        }

        val requestCode = getRequestCode(doseEvent.id)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(snoozeTime, pendingIntent), pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
        }
    }

    fun showNotification(context: Context, medId: Int, medName: String, doseEventId: Int, hour: Int, minute: Int) {
        createNotificationChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = "android.resource://${context.packageName}/${R.raw.med_reminder}".toUri()

        val notificationId = getRequestCode(doseEventId)

        val openIntent = PendingIntent.getActivity(
            context, notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("medicationId", medId)
                putExtra(KEY_DOSE_ID, doseEventId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takenAction = PendingIntent.getBroadcast(
            context, notificationId + 1_000_000,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_TAKEN
                putExtra(KEY_MED_ID,   medId)
                putExtra(KEY_MED_NAME, medName)
                putExtra(KEY_DOSE_ID,  doseEventId)
                putExtra(KEY_HOUR,     hour)
                putExtra(KEY_MINUTE,   minute)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeAction = PendingIntent.getBroadcast(
            context, notificationId + 2_000_000,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_SNOOZE
                putExtra(KEY_MED_ID,   medId)
                putExtra(KEY_MED_NAME, medName)
                putExtra(KEY_DOSE_ID,  doseEventId)
                putExtra(KEY_HOUR,     hour)
                putExtra(KEY_MINUTE,   minute)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time for $medName")
            .setContentText("Take your $medName now")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's time to take your $medName.\n\nTap 'Mark Taken' to confirm or snooze for 10 minutes.")
                .setBigContentTitle("Medication Reminder"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 600))
            .setColor(0xFF2196F3.toInt())
            .addAction(android.R.drawable.checkbox_on_background, "Mark Taken", takenAction)
            .addAction(android.R.drawable.ic_menu_recent_history, "Snooze 10min", snoozeAction)
            .build()

        manager.notify(notificationId, notification)
    }
}
