package ca.sheridancollege.medreminder.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import ca.sheridancollege.medreminder.data.local.dao.DoseEventDao
import ca.sheridancollege.medreminder.data.local.dao.IntakeLogDao
import ca.sheridancollege.medreminder.data.local.dao.MedicationDao
import ca.sheridancollege.medreminder.data.repository.FirestoreSyncRepository
import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.domain.model.IntakeLog
import ca.sheridancollege.medreminder.domain.model.Medication
import ca.sheridancollege.medreminder.domain.model.MedicationTime
import ca.sheridancollege.medreminder.domain.model.MedicationType
import ca.sheridancollege.medreminder.domain.model.PillShape
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val medicationDao: MedicationDao,
    private val intakeLogDao: IntakeLogDao,
    private val doseEventDao: DoseEventDao,
    private val firestoreSyncRepository: FirestoreSyncRepository
) : CoroutineWorker(context, workerParams) {

    private val gson = Gson()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Upload unsynced Medications
            val unsyncedMeds = medicationDao.getUnsyncedMedications()
            unsyncedMeds.forEach { entity ->
                firestoreSyncRepository.uploadMedication(entity.toDomain())
                medicationDao.update(entity.copy(isSynced = true))
            }

            // 2. Upload unsynced Logs
            val unsyncedLogs = intakeLogDao.getUnsyncedLogs()
            unsyncedLogs.forEach { entity ->
                firestoreSyncRepository.uploadIntakeLog(entity.toDomain())
                intakeLogDao.update(entity.copy(isSynced = true))
            }

            // 3. Upload unsynced DoseEvents
            val unsyncedEvents = doseEventDao.getUnsyncedEvents()
            unsyncedEvents.forEach { entity ->
                // firestoreSyncRepository.uploadDoseEvent(entity.toDomain())
                doseEventDao.update(entity.copy(isSynced = true))
            }

            // 4. Pull from remote
            val remoteMeds = firestoreSyncRepository.fetchMedications()
            medicationDao.upsertFromSync(remoteMeds.map { it.toEntity() })
            
            val remoteLogs = firestoreSyncRepository.fetchIntakeLogs()
            intakeLogDao.upsertFromSync(remoteLogs.map { it.toEntity() })

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun Medication.toEntity() = ca.sheridancollege.medreminder.data.local.entity.MedicationEntity(
        id = id,
        name = name,
        dosageAmount = dosageAmount,
        dosageUnit = dosageUnit,
        medicationType = medicationType.name,
        timesJson = gson.toJson(times),
        days = DayOfWeek.toCodes(days),
        startDate = startDate,
        endDate = endDate,
        instructions = instructions,
        isAsNeeded = isAsNeeded,
        maxPerDay = maxPerDay,
        isActive = isActive,
        notes = notes,
        pillColor = pillColor,
        pillShape = pillShape.name,
        stockQuantity = stockQuantity,
        remainingQuantity = remainingQuantity,
        refillThreshold = refillThreshold,
        nfcTagId = nfcTagId,
        updatedAt = updatedAt,
        isSynced = true
    )

    private fun ca.sheridancollege.medreminder.data.local.entity.MedicationEntity.toDomain(): Medication {
        val timesType = object : TypeToken<List<MedicationTime>>() {}.type
        val times: List<MedicationTime> = gson.fromJson(timesJson, timesType) ?: emptyList()
        return Medication(
            id = id,
            name = name,
            dosageAmount = dosageAmount,
            dosageUnit = dosageUnit,
            medicationType = MedicationType.valueOf(medicationType),
            times = times,
            days = DayOfWeek.fromCodes(days),
            startDate = startDate,
            endDate = endDate,
            instructions = instructions,
            isAsNeeded = isAsNeeded,
            maxPerDay = maxPerDay,
            isActive = isActive,
            notes = notes,
            pillColor = pillColor,
            pillShape = PillShape.valueOf(pillShape),
            stockQuantity = stockQuantity,
            remainingQuantity = remainingQuantity,
            refillThreshold = refillThreshold,
            nfcTagId = nfcTagId,
            updatedAt = updatedAt
        )
    }

    private fun ca.sheridancollege.medreminder.data.local.entity.IntakeLogEntity.toDomain() = IntakeLog(
        id = id,
        medicationId = medicationId,
        medicationName = medicationName,
        takenAt = takenAt,
        scheduledHour = scheduledHour,
        scheduledMinute = scheduledMinute,
        wasOnTime = wasOnTime,
        snoozeReason = snoozeReason,
        wasDoubleDoseAttempt = wasDoubleDoseAttempt,
        updatedAt = updatedAt
    )

    private fun IntakeLog.toEntity() = ca.sheridancollege.medreminder.data.local.entity.IntakeLogEntity(
        id = id,
        medicationId = medicationId,
        medicationName = medicationName,
        takenAt = takenAt,
        scheduledHour = scheduledHour,
        scheduledMinute = scheduledMinute,
        wasOnTime = wasOnTime,
        snoozeReason = snoozeReason,
        wasDoubleDoseAttempt = wasDoubleDoseAttempt,
        updatedAt = updatedAt,
        isSynced = true
    )

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "FirestoreSync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
        
        fun startImmediate(context: Context) {
             val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
