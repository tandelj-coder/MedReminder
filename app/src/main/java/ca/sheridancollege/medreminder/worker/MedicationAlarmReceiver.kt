package ca.sheridancollege.medreminder.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medId   = intent.getIntExtra(MedicationAlarmScheduler.KEY_MED_ID, 0)
        val medName = intent.getStringExtra(MedicationAlarmScheduler.KEY_MED_NAME) ?: return
        val doseId  = intent.getIntExtra(MedicationAlarmScheduler.KEY_DOSE_ID, 0)
        val hour    = intent.getIntExtra(MedicationAlarmScheduler.KEY_HOUR, 0)
        val minute  = intent.getIntExtra(MedicationAlarmScheduler.KEY_MINUTE, 0)

        if (doseId != 0) {
            MedicationAlarmScheduler.showNotification(context, medId, medName, doseId, hour, minute)
        }
        
        // No longer rescheduling for tomorrow here because DoseEventGenerationWorker handles it
    }
}
