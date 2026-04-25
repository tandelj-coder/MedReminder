package ca.sheridancollege.medreminder.worker

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ca.sheridancollege.medreminder.data.repository.DoseEventRepository
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
    
    @Inject
    lateinit var doseEventRepository: DoseEventRepository

    companion object {
        const val ACTION_MARK_TAKEN = "ca.sheridancollege.medreminder.MARK_TAKEN"
        const val ACTION_SNOOZE     = "ca.sheridancollege.medreminder.SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val doseId  = intent.getIntExtra(MedicationAlarmScheduler.KEY_DOSE_ID, 0)
        val medId   = intent.getIntExtra(MedicationAlarmScheduler.KEY_MED_ID, 0)
        val hour    = intent.getIntExtra(MedicationAlarmScheduler.KEY_HOUR, -1)
        val minute  = intent.getIntExtra(MedicationAlarmScheduler.KEY_MINUTE, -1)

        // Dismiss the notification
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(MedicationAlarmScheduler.getRequestCode(doseId))

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
                CoroutineScope(Dispatchers.IO).launch {
                    val doseEvent = doseEventRepository.getDoseEventById(doseId)
                    if (doseEvent != null) {
                        MedicationAlarmScheduler.snooze(context, doseEvent)
                    }
                }
            }
        }
    }
}
