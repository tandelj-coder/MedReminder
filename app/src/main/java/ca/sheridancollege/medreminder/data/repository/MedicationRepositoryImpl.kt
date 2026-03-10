package ca.sheridancollege.medreminder.data.repository

import ca.sheridancollege.medreminder.data.local.dao.IntakeLogDao
import ca.sheridancollege.medreminder.data.local.dao.MedicationDao
import ca.sheridancollege.medreminder.data.local.entity.IntakeLogEntity
import ca.sheridancollege.medreminder.data.local.entity.MedicationEntity
import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.domain.model.IntakeLog
import ca.sheridancollege.medreminder.domain.model.Medication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao,
    private val intakeLogDao: IntakeLogDao,
    private val firestoreSyncRepository: FirestoreSyncRepository
) : MedicationRepository {

    // ─── Medications ────────────────────────────────────────────────

    override fun getAllActiveMedications(): Flow<List<Medication>> =
        medicationDao.getAllActiveMedications().map { list ->
            list.map { it.toDomain() }
        }

    override fun getMedicationsForDay(dayCode: String): Flow<List<Medication>> =
        medicationDao.getMedicationsForDay(dayCode).map { list ->
            list.map { it.toDomain() }
        }

    override fun getTakenCountToday(): Flow<Int> =
        medicationDao.getTakenCountToday()

    override fun getTotalActiveCount(): Flow<Int> =
        medicationDao.getTotalActiveCount()

    override suspend fun getMedicationById(id: Int): Medication? =
        medicationDao.getMedicationById(id)?.toDomain()

    override suspend fun insertMedication(medication: Medication): Long {
        val id = medicationDao.insert(medication.toEntity())
        val updatedMedication = medication.copy(id = id.toInt())
        firestoreSyncRepository.uploadMedication(updatedMedication)
        return id
    }

    override suspend fun updateMedication(medication: Medication) {
        medicationDao.update(medication.toEntity())
        firestoreSyncRepository.uploadMedication(medication)
    }

    override suspend fun deleteMedication(medication: Medication) {
        medicationDao.delete(medication.toEntity())
        intakeLogDao.deleteLogsForMedication(medication.id)
        firestoreSyncRepository.deleteMedication(medication.id)
    }

    override suspend fun markAsTaken(
        medicationId: Int,
        timestamp: Long,
        scheduledHour: Int,
        scheduledMinute: Int
    ) {
        medicationDao.markAsTaken(medicationId, timestamp)

        val scheduled = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, scheduledHour)
            set(Calendar.MINUTE, scheduledMinute)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        val minutesDiff = Math.abs(timestamp - scheduled) / 60000
        val wasOnTime = minutesDiff <= 60

        intakeLogDao.insert(
            IntakeLogEntity(
                medicationId = medicationId,
                medicationName = medicationDao.getMedicationById(medicationId)?.name ?: "",
                takenAt = timestamp,
                scheduledHour = scheduledHour,
                scheduledMinute = scheduledMinute,
                wasOnTime = wasOnTime
            )
        )
        
        // Also sync the updated medication status to Firestore
        medicationDao.getMedicationById(medicationId)?.let {
            firestoreSyncRepository.uploadMedication(it.toDomain())
        }
    }

    override suspend fun resetAllDailyStatus() =
        medicationDao.resetAllDailyStatus()

    // ─── Logs ───────────────────────────────────────────────────────

    override fun getAllLogs(): Flow<List<IntakeLog>> =
        intakeLogDao.getAllLogs().map { list ->
            list.map { it.toDomain() }
        }

    override fun getLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<IntakeLog>> =
        intakeLogDao.getLogsForDay(startOfDay, endOfDay).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getLastIntakeForMedication(medicationId: Int): IntakeLog? =
        intakeLogDao.getLastIntakeForMedication(medicationId)?.toDomain()

    // ─── Mappers ────────────────────────────────────────────────────

    private fun MedicationEntity.toDomain() = Medication(
        id = id,
        name = name,
        dosage = dosage,
        timeHour = timeHour,
        timeMinute = timeMinute,
        days = DayOfWeek.fromCodes(days),
        isTakenToday = isTakenToday,
        takenTimestamp = takenTimestamp,
        isActive = isActive,
        notes = notes
    )

    private fun Medication.toEntity() = MedicationEntity(
        id = id,
        name = name,
        dosage = dosage,
        timeHour = timeHour,
        timeMinute = timeMinute,
        days = DayOfWeek.toCodes(days),
        isTakenToday = isTakenToday,
        takenTimestamp = takenTimestamp,
        isActive = isActive,
        notes = notes
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
        wasDoubleDoseAttempt = wasDoubleDoseAttempt
    )
}
