package ca.sheridancollege.medreminder.worker

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: MedicationRepository

    companion object {
        const val ACTION_MARK_TAKEN = "ca.sheridancollege.medreminder.MARK_TAKEN"
        const val ACTION_SNOOZE     = "ca.sheridancollege.medreminder.SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medId   = intent.getIntExtra(MedicationAlarmScheduler.KEY_MED_ID, 0)
        val medName = intent.getStringExtra(MedicationAlarmScheduler.KEY_MED_NAME) ?: ""
        val dosage  = intent.getStringExtra(MedicationAlarmScheduler.KEY_MED_DOSAGE) ?: ""
        val hour    = intent.getIntExtra(MedicationAlarmScheduler.KEY_HOUR, -1)
        val minute  = intent.getIntExtra(MedicationAlarmScheduler.KEY_MINUTE, -1)

        // Dismiss the notification
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(medId)

        when (intent.action) {
            ACTION_MARK_TAKEN -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        repository.markAsTaken(
                            medicationId = medId,
                            timestamp = System.currentTimeMillis(),
                            scheduledHour = hour,
                            scheduledMinute = minute
                        )
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
                        putExtra(MedicationAlarmScheduler.KEY_HOUR, hour)
                        putExtra(MedicationAlarmScheduler.KEY_MINUTE, minute)
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
