package ca.sheridancollege.medreminder.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import ca.sheridancollege.medreminder.data.repository.DoseEventRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DoseMissedDetectionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val doseEventRepository: DoseEventRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "dose_missed_detection"

        fun schedule(context: Context) {
            val now = java.util.Calendar.getInstance()
            val midnight = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 1)
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            val delay = midnight.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<DoseMissedDetectionWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    TAG,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }

        fun scheduleImmediateRun(context: Context) {
            val request = OneTimeWorkRequestBuilder<DoseMissedDetectionWorker>()
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    TAG + "_immediate",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val cutoffTime = System.currentTimeMillis()
            doseEventRepository.markOverdueAssMissed(cutoffTime)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
