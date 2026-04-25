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
import ca.sheridancollege.medreminder.data.repository.DoseEventRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class DoseEventReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val doseEventRepository: DoseEventRepository,
    private val userPreferences: UserPreferencesDataStore
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_DOSE_EVENT_ID = "dose_event_id"
        const val KEY_MED_NAME = "med_name"
        const val KEY_MED_DOSAGE = "med_dosage"
        const val CHANNEL_ID = "medication_reminders"

        fun schedule(
            context: Context,
            doseEventId: Int,
            medicationName: String,
            dosage: String,
            scheduledTime: Long
        ) {
            val now = System.currentTimeMillis()
            val delay = (scheduledTime - now).coerceAtLeast(0)

            val data = workDataOf(
                KEY_DOSE_EVENT_ID to doseEventId,
                KEY_MED_NAME to medicationName,
                KEY_MED_DOSAGE to dosage
            )

            val request = OneTimeWorkRequestBuilder<DoseEventReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("dose_reminder_$doseEventId")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "dose_reminder_$doseEventId",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }

        fun cancel(context: Context, doseEventId: Int) {
            WorkManager.getInstance(context)
                .cancelUniqueWork("dose_reminder_$doseEventId")
        }
    }

    override suspend fun doWork(): Result {
        val isEnabled = userPreferences.notificationsEnabled.first()
        if (!isEnabled) {
            return Result.success()
        }

        val doseEventId = inputData.getInt(KEY_DOSE_EVENT_ID, -1)
        if (doseEventId == -1) {
            return Result.failure()
        }

        val medName = inputData.getString(KEY_MED_NAME) ?: return Result.failure()
        val dosage = inputData.getString(KEY_MED_DOSAGE) ?: ""

        showNotification(doseEventId, medName, dosage)
        return Result.success()
    }

    private fun showNotification(doseEventId: Int, medName: String, dosage: String) {
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
            putExtra("doseEventId", doseEventId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, doseEventId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💊 Time for $medName")
            .setContentText("$dosage — Did you actually take it?")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's time to take $medName ($dosage). Open the app to confirm you took it and avoid double-dosing."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(doseEventId, notification)
    }
}
