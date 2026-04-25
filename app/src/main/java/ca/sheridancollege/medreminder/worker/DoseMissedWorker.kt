package ca.sheridancollege.medreminder.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.sheridancollege.medreminder.data.repository.DoseEventRepository
import ca.sheridancollege.medreminder.domain.model.DoseStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DoseMissedWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val doseEventRepository: DoseEventRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_DOSE_EVENT_ID = "dose_event_id"
    }

    override suspend fun doWork(): Result {
        val doseEventId = inputData.getInt(KEY_DOSE_EVENT_ID, 0)
        if (doseEventId == 0) {
            return Result.failure()
        }

        val doseEvent = doseEventRepository.getDoseEventById(doseEventId)

        if (doseEvent != null && doseEvent.status == DoseStatus.SCHEDULED) {
            doseEventRepository.markDoseAsMissed(doseEvent.id)
        }

        return Result.success()
    }
}
