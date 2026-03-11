package ca.sheridancollege.medreminder.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medId   = intent.getIntExtra(MedicationAlarmScheduler.KEY_MED_ID, 0)
        val medName = intent.getStringExtra(MedicationAlarmScheduler.KEY_MED_NAME) ?: return
        val dosage  = intent.getStringExtra(MedicationAlarmScheduler.KEY_MED_DOSAGE) ?: ""
        val hour    = intent.getIntExtra(MedicationAlarmScheduler.KEY_HOUR, 0)
        val minute  = intent.getIntExtra(MedicationAlarmScheduler.KEY_MINUTE, 0)

        MedicationAlarmScheduler.showNotification(context, medId, medName, dosage)

        // Reschedule for tomorrow
        if (hour >= 0) {
            MedicationAlarmScheduler.schedule(context, medId, medName, dosage, hour, minute)
        }
    }
}
