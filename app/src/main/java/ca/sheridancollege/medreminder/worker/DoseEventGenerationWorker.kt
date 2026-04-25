package ca.sheridancollege.medreminder.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import ca.sheridancollege.medreminder.data.repository.DoseEventRepository
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DoseEventGenerationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val doseEventRepository: DoseEventRepository,
    private val medicationRepository: MedicationRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "dose_event_generation"

        fun scheduleOnAppStart(context: Context) {
            val request = OneTimeWorkRequestBuilder<DoseEventGenerationWorker>()
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    TAG,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }

        fun scheduleDaily(context: Context) {
            val now = java.util.Calendar.getInstance()
            val midnight = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            val delay = midnight.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<DoseEventGenerationWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    TAG + "_periodic",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            medicationRepository.generateDoseEventsForToday()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
