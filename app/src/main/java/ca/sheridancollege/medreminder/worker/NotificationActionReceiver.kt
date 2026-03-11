package ca.sheridancollege.medreminder.worker

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_TAKEN = "ca.sheridancollege.medreminder.MARK_TAKEN"
        const val ACTION_SNOOZE     = "ca.sheridancollege.medreminder.SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medId   = intent.getIntExtra(MedicationAlarmScheduler.KEY_MED_ID, 0)
        val medName = intent.getStringExtra(MedicationAlarmScheduler.KEY_MED_NAME) ?: ""
        val dosage  = intent.getStringExtra(MedicationAlarmScheduler.KEY_MED_DOSAGE) ?: ""

        // Dismiss the notification
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(medId)

        when (intent.action) {
            ACTION_MARK_TAKEN -> {
                // Mark as taken in DB directly
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = ca.sheridancollege.medreminder.data.local.MedicationDatabase::class.java
                            .let { androidx.room.Room.databaseBuilder(context, it, "medication_db")
                                .fallbackToDestructiveMigration().build() }
                        db.medicationDao().markAsTaken(medId, System.currentTimeMillis())
                        db.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            ACTION_SNOOZE -> {
                // Fire again in 10 minutes using AlarmManager
                val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE)
                    as android.app.AlarmManager
                val snoozeIntent = android.app.PendingIntent.getBroadcast(
                    context, medId + 30000,
                    Intent(context, MedicationAlarmReceiver::class.java).apply {
                        putExtra(MedicationAlarmScheduler.KEY_MED_ID, medId)
                        putExtra(MedicationAlarmScheduler.KEY_MED_NAME, medName)
                        putExtra(MedicationAlarmScheduler.KEY_MED_DOSAGE, dosage)
                        putExtra(MedicationAlarmScheduler.KEY_HOUR, -1)
                        putExtra(MedicationAlarmScheduler.KEY_MINUTE, -1)
                    },
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP, snoozeTime, snoozeIntent
                )
            }
        }
    }
}
