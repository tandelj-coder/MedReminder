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
    const val KEY_DOSE_ID     = "dose_id"
    const val KEY_HOUR        = "hour"
    const val KEY_MINUTE      = "minute"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return

            val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.med_reminder}")
            val audioAttr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(CHANNEL_ID, "Race Control Reminders",
                NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Urgent pit stop reminders from Race Control"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 600)
                setSound(soundUri, audioAttr)
                enableLights(true)
                lightColor = android.graphics.Color.RED
                setBypassDnd(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun getRequestCode(doseEventId: Int): Int {
        return doseEventId // Use doseEventId directly as it's unique
    }

    fun schedule(
        context: Context,
        doseEvent: ca.sheridancollege.medreminder.domain.model.DoseEvent
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(context, MedicationAlarmReceiver::class.java).also {
            it.putExtra(KEY_MED_ID,    doseEvent.medicationId)
            it.putExtra(KEY_MED_NAME,  doseEvent.medicationName)
            it.putExtra(KEY_DOSE_ID,   doseEvent.id)
            it.putExtra(KEY_HOUR,      Calendar.getInstance().apply { timeInMillis = doseEvent.scheduledTime }.get(Calendar.HOUR_OF_DAY))
            it.putExtra(KEY_MINUTE,    Calendar.getInstance().apply { timeInMillis = doseEvent.scheduledTime }.get(Calendar.MINUTE))
        }

        val requestCode = getRequestCode(doseEvent.id)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = doseEvent.scheduledTime

        if (triggerTime < System.currentTimeMillis()) {
            // Don't schedule past alarms
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelAllForMedication(context: Context, medicationId: Int) {
        // This method needs to be refactored to cancel based on doseEventIds associated with the medication
        // For now, we'll keep a placeholder or remove it if not used
        // This might need a new repository method to get all scheduled dose events for a medication
        // The old implementation based on times is not suitable with unique dose events.
        // If a medication is deleted, we should delete its future dose events and cancel their alarms.
        // This will be handled in MedicationRepositoryImpl's deleteMedication method
    }

    // Old cancel method is no longer applicable with unique dose event IDs
    // fun cancel(context: Context, medicationId: Int, hour: Int, minute: Int) {
    //     val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    //     val intent = Intent(context, MedicationAlarmReceiver::class.java)
    //     val requestCode = getRequestCode(medicationId, hour, minute)
    //     val pendingIntent = PendingIntent.getBroadcast(
    //         context,
    //         requestCode,
    //         intent,
    //         PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    //     )
    //     alarmManager.cancel(pendingIntent)
    // }

    fun cancel(context: Context, doseEventId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicationAlarmReceiver::class.java)
        val requestCode = getRequestCode(doseEventId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun snooze(context: Context, doseEvent: ca.sheridancollege.medreminder.domain.model.DoseEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000L // 10 minutes from now

        val alarmIntent = Intent(context, MedicationAlarmReceiver::class.java).also {
            it.putExtra(KEY_MED_ID, doseEvent.medicationId)
            it.putExtra(KEY_MED_NAME, doseEvent.medicationName)
            it.putExtra(KEY_DOSE_ID, doseEvent.id)
            it.putExtra(KEY_HOUR, Calendar.getInstance().apply { timeInMillis = doseEvent.scheduledTime }.get(Calendar.HOUR_OF_DAY))
            it.putExtra(KEY_MINUTE, Calendar.getInstance().apply { timeInMillis = doseEvent.scheduledTime }.get(Calendar.MINUTE))
        }

        val requestCode = getRequestCode(doseEvent.id) // Use the same request code for snooze
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(snoozeTime, pendingIntent),
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
        }
    }

    fun showNotification(context: Context, medId: Int, medName: String, doseEventId: Int, hour: Int, minute: Int) {
        createNotificationChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.med_reminder}")

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

        val takenAction = run {
            val i = Intent(context, NotificationActionReceiver::class.java)
            i.action = NotificationActionReceiver.ACTION_MARK_TAKEN
            i.putExtra(KEY_MED_ID, medId)
            i.putExtra(KEY_MED_NAME, medName)
            i.putExtra(KEY_DOSE_ID, doseEventId)
            i.putExtra(KEY_HOUR, hour)
            i.putExtra(KEY_MINUTE, minute)
            PendingIntent.getBroadcast(
                context, notificationId + 1000000, i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val snoozeAction = run {
            val i = Intent(context, NotificationActionReceiver::class.java)
            i.action = NotificationActionReceiver.ACTION_SNOOZE
            i.putExtra(KEY_MED_ID, medId)
            i.putExtra(KEY_MED_NAME, medName)
            i.putExtra(KEY_DOSE_ID, doseEventId)
            i.putExtra(KEY_HOUR, hour)
            i.putExtra(KEY_MINUTE, minute)
            PendingIntent.getBroadcast(
                context, notificationId + 2000000, i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Fetch dosage information from Medication (if needed in notification text)
        // For now, removing dosage text as it's not directly in DoseEvent
        // val dosageText = if (dosage.isNotBlank()) " · $dosage" else ""
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💊 Time for $medName")
            .setContentText("Take your $medName now")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's time to take your $medName.\n\nTap 'Mark Taken' to confirm or snooze for 10 minutes.")
                .setBigContentTitle("💊 Medication Reminder"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 600))
            .setColor(0xFF00BCD4.toInt())
            .addAction(android.R.drawable.checkbox_on_background, "✓ Mark Taken", takenAction)
            .addAction(android.R.drawable.ic_menu_recent_history, "⏰ Snooze 10min", snoozeAction)
            .setFullScreenIntent(openIntent, true)
            .build()

        manager.notify(notificationId, notification)
    }
}
