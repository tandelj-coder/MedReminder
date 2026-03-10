package ca.sheridancollege.medreminder.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import ca.sheridancollege.medreminder.MainActivity
import ca.sheridancollege.medreminder.data.datastore.UserPreferencesDataStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class MedicationReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val userPreferences: UserPreferencesDataStore
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_MED_ID = "med_id"
        const val KEY_MED_NAME = "med_name"
        const val KEY_MED_DOSAGE = "med_dosage"
        const val CHANNEL_ID = "medication_reminders"

        fun schedule(
            context: Context,
            medicationId: Int,
            medicationName: String,
            dosage: String,
            hour: Int,
            minute: Int
        ) {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            if (target.before(now)) target.add(java.util.Calendar.DAY_OF_MONTH, 1)

            val delay = target.timeInMillis - now.timeInMillis
            val data = workDataOf(
                KEY_MED_ID to medicationId,
                KEY_MED_NAME to medicationName,
                KEY_MED_DOSAGE to dosage
            )

            val request = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("reminder_$medicationId")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "reminder_$medicationId",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }

        fun cancel(context: Context, medicationId: Int) {
            WorkManager.getInstance(context)
                .cancelUniqueWork("reminder_$medicationId")
        }
    }

    override suspend fun doWork(): Result {
        // Check if notifications are enabled in settings
        val isEnabled = userPreferences.notificationsEnabled.first()
        if (!isEnabled) {
            return Result.success()
        }

        val medName = inputData.getString(KEY_MED_NAME) ?: return Result.failure()
        val dosage = inputData.getString(KEY_MED_DOSAGE) ?: ""
        showNotification(medName, dosage)
        return Result.success()
    }

    private fun showNotification(medName: String, dosage: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to take your medication"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time for $medName")
            .setContentText("$dosage — Did you actually take it?")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's time to take $medName ($dosage). Open the app to confirm you took it and avoid double-dosing."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
