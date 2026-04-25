package ca.sheridancollege.medreminder.data.repository

import ca.sheridancollege.medreminder.data.local.dao.IntakeLogDao
import ca.sheridancollege.medreminder.data.local.dao.MedicationDao
import ca.sheridancollege.medreminder.data.local.entity.IntakeLogEntity
import ca.sheridancollege.medreminder.data.local.entity.MedicationEntity
import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.domain.model.DoseEvent
import ca.sheridancollege.medreminder.domain.model.DoseStatus
import ca.sheridancollege.medreminder.domain.model.IntakeLog
import ca.sheridancollege.medreminder.domain.model.Medication
import ca.sheridancollege.medreminder.domain.model.MedicationTime
import ca.sheridancollege.medreminder.domain.model.MedicationType
import ca.sheridancollege.medreminder.domain.model.PillShape
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao,
    private val intakeLogDao: IntakeLogDao,
    private val doseEventRepository: DoseEventRepository,
    private val firestoreSyncRepository: FirestoreSyncRepository
) : MedicationRepository {

    private val gson = Gson()

    // ─── Medications ────────────────────────────────────────────────

    override fun getAllActiveMedications(): Flow<List<Medication>> =
        medicationDao.getAllActiveMedications().map { list ->
            list.map { it.toDomain() }
        }

    override fun getMedicationsForDay(dayCode: String): Flow<List<Medication>> {
        val (startOfDay, endOfDay) = todayRange()
        return medicationDao.getMedicationsForDay(dayCode)
            .combine(intakeLogDao.getLogsForDay(startOfDay, endOfDay)) { meds, _ ->
                meds.map { entity -> entity.toDomain() }
            }
    }

    override fun getTakenCountToday(): Flow<Int> {
        val (startOfDay, endOfDay) = todayRange()
        return intakeLogDao.getTakenCountForDay(startOfDay, endOfDay)
    }

    override fun getTotalActiveCount(): Flow<Int> =
        medicationDao.getTotalActiveCount()

    override suspend fun getMedicationById(id: Int): Medication? =
        medicationDao.getMedicationById(id)?.toDomain()

    override suspend fun insertMedication(medication: Medication): Long {
        val medicationWithTimestamp = medication.copy(updatedAt = System.currentTimeMillis())
        val id = medicationDao.insert(medicationWithTimestamp.toEntity().copy(isSynced = false))
        val finalMedication = medicationWithTimestamp.copy(id = id.toInt())
        
        try {
            firestoreSyncRepository.uploadMedication(finalMedication)
            medicationDao.update(finalMedication.toEntity().copy(isSynced = true))
        } catch (_: Exception) { }
        return id
    }

    override suspend fun updateMedication(medication: Medication) {
        val updatedMedication = medication.copy(updatedAt = System.currentTimeMillis())
        medicationDao.update(updatedMedication.toEntity().copy(isSynced = false))
        
        try {
            firestoreSyncRepository.uploadMedication(updatedMedication)
            medicationDao.update(updatedMedication.toEntity().copy(isSynced = true))
        } catch (_: Exception) { }
    }

    override suspend fun deleteMedication(medication: Medication) {
        medicationDao.delete(medication.toEntity())
        intakeLogDao.deleteLogsForMedication(medication.id)
        doseEventRepository.deleteUnresolvedEventsForMedication(medication.id)
        firestoreSyncRepository.deleteMedication(medication.id)
    }

    override suspend fun markAsTaken(
        medicationId: Int,
        timestamp: Long,
        scheduledHour: Int,
        scheduledMinute: Int,
        doseEventId: Int?
    ) {
        val entity = medicationDao.getMedicationById(medicationId) ?: return

        if (entity.stockQuantity > 0) {
            medicationDao.update(
                entity.copy(remainingQuantity = (entity.remainingQuantity - 1).coerceAtLeast(0))
            )
        }

        val scheduled = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, scheduledHour)
            set(Calendar.MINUTE, scheduledMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val wasOnTime = Math.abs(timestamp - scheduled) / 60000 <= 60

        val log = IntakeLog(
            medicationId = medicationId,
            medicationName = entity.name,
            takenAt = timestamp,
            scheduledHour = scheduledHour,
            scheduledMinute = scheduledMinute,
            wasOnTime = wasOnTime,
            updatedAt = System.currentTimeMillis()
        )
        intakeLogDao.insert(log.toEntity().copy(isSynced = false))

        if (doseEventId != null) {
            doseEventRepository.markDoseAsTaken(doseEventId, timestamp)
        } else {
            markDoseEventAsTaken(medicationId, timestamp, scheduledHour, scheduledMinute)
        }

        try {
            firestoreSyncRepository.uploadIntakeLog(log)
        } catch (_: Exception) { }
    }

    private suspend fun markDoseEventAsTaken(medicationId: Int, takenAt: Long, hour: Int, minute: Int) {
        val (startOfDay, endOfDay) = todayRange()
        try {
            val events = doseEventRepository
                .getDoseEventsForMedicationOnDay(medicationId, startOfDay, endOfDay)
                .first()
            val doseEvent = events.firstOrNull { event ->
                val cal = Calendar.getInstance().apply { timeInMillis = event.scheduledTime }
                event.isScheduled() &&
                    cal.get(Calendar.HOUR_OF_DAY) == hour &&
                    cal.get(Calendar.MINUTE) == minute
            } ?: return
            doseEventRepository.markDoseAsTaken(doseEvent.id, takenAt)
        } catch (_: Exception) { }
    }

    override suspend fun markAsSkipped(
        medicationId: Int,
        scheduledHour: Int,
        scheduledMinute: Int
    ) {
        val (startOfDay, endOfDay) = todayRange()
        try {
            val events = doseEventRepository
                .getDoseEventsForMedicationOnDay(medicationId, startOfDay, endOfDay)
                .first()
            val doseEvent = events.firstOrNull { event ->
                val cal = Calendar.getInstance().apply { timeInMillis = event.scheduledTime }
                event.isScheduled() &&
                    cal.get(Calendar.HOUR_OF_DAY) == scheduledHour &&
                    cal.get(Calendar.MINUTE) == scheduledMinute
            } ?: return
            doseEventRepository.updateDoseStatus(doseEvent.id, DoseStatus.SKIPPED)
        } catch (_: Exception) { }
    }

    override suspend fun resetAllDailyStatus() {
        val (startOfDay, endOfDay) = todayRange()
        intakeLogDao.deleteLogsForDay(startOfDay, endOfDay)
        resetDoseEventsForDay(startOfDay, endOfDay)
    }

    override suspend fun resetDoseEventsForDay(startOfDay: Long, endOfDay: Long) {
        val events = doseEventRepository.getDoseEventsForDay(startOfDay, endOfDay).first()
        for (event in events) {
            doseEventRepository.updateDoseStatus(event.id, DoseStatus.SCHEDULED)
        }
    }

    override fun getAllLogs(): Flow<List<IntakeLog>> =
        intakeLogDao.getAllLogs().map { list -> list.map { it.toDomain() } }

    override fun getLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<IntakeLog>> =
        intakeLogDao.getLogsForDay(startOfDay, endOfDay).map { list -> list.map { it.toDomain() } }

    override suspend fun getLastIntakeForMedication(medicationId: Int): IntakeLog? =
        intakeLogDao.getLastIntakeForMedication(medicationId)?.toDomain()

    override fun getAllDoseEvents(): Flow<List<DoseEvent>> =
        doseEventRepository.getAllEvents()

    override suspend fun syncFromRemote(medications: List<Medication>, logs: List<IntakeLog>) {
        medicationDao.upsertFromSync(medications.map { it.toEntity() })
        intakeLogDao.upsertFromSync(logs.map { it.toEntity() })
    }

    override fun getTodayDoseEvents(startOfDay: Long, endOfDay: Long): Flow<List<DoseEvent>> =
        doseEventRepository.getDoseEventsForDay(startOfDay, endOfDay)

    override suspend fun generateDoseEventsForToday(): Result<Int> {
        val medicationList = medicationDao.getAllActiveMedications().first()
        var totalCreated = 0

        for (medication in medicationList) {
            val result = doseEventRepository.generateFutureEvents(medication.id, daysAhead = 1)
            if (result.isSuccess) {
                totalCreated += result.getOrDefault(0)
            }
        }

        return Result.success(totalCreated)
    }

    private fun todayRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return start to (start + 24 * 60 * 60 * 1000L - 1)
    }

    // ─── Mappers ────────────────────────────────────────────────────

    private fun MedicationEntity.toDomain(): Medication {
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

    private fun Medication.toEntity() = MedicationEntity(
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
        updatedAt = updatedAt
    )

    private fun IntakeLogEntity.toDomain() = IntakeLog(
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

    private fun IntakeLog.toEntity() = IntakeLogEntity(
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
}
