package ca.sheridancollege.medreminder.worker

import ca.sheridancollege.medreminder.data.datastore.UserPreferencesDataStore
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyResetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: MedicationRepository,
    private val dataStore: UserPreferencesDataStore
) : CoroutineWorker(context, params) {

    companion object {
        fun schedule(context: Context) {
            val now = java.util.Calendar.getInstance()
            val midnight = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            val delay = midnight.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<DailyResetWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_reset",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            repository.resetAllDailyStatus()
            repository.generateDoseEventsForToday()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
