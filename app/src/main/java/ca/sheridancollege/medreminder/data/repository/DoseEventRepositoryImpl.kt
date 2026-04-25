package ca.sheridancollege.medreminder.data.repository

import ca.sheridancollege.medreminder.data.local.dao.DoseEventDao
import ca.sheridancollege.medreminder.data.local.dao.MedicationDao
import ca.sheridancollege.medreminder.data.local.entity.DoseEventEntity
import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.domain.model.DoseEvent
import ca.sheridancollege.medreminder.domain.model.DoseStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoseEventRepositoryImpl @Inject constructor(
    private val doseEventDao: DoseEventDao,
    private val medicationDao: MedicationDao,
    private val firestoreSyncRepository: FirestoreSyncRepository
) : DoseEventRepository {

    override suspend fun createDoseEvent(
        medicationId: Int,
        medicationName: String,
        scheduledTime: Long
    ): Long {
        val entity = DoseEventEntity(
            medicationId = medicationId,
            medicationName = medicationName,
            scheduledTime = scheduledTime,
            status = DoseStatus.SCHEDULED.name,
            isSynced = false
        )
        return doseEventDao.insert(entity)
    }

    override suspend fun getDoseEventById(id: Int): DoseEvent? =
        doseEventDao.getById(id)?.toDomain()

    override fun getDoseEventsForDay(startOfDay: Long, endOfDay: Long): Flow<List<DoseEvent>> =
        doseEventDao.getEventsForDay(startOfDay, endOfDay).map { events ->
            events.map { it.toDomain() }
        }

    override fun getDoseEventsForMedicationOnDay(
        medicationId: Int,
        startOfDay: Long,
        endOfDay: Long
    ): Flow<List<DoseEvent>> =
        doseEventDao.getEventsForMedicationOnDay(medicationId, startOfDay, endOfDay).map { events ->
            events.map { it.toDomain() }
        }

    override fun getNextScheduledEvents(fromTime: Long): Flow<List<DoseEvent>> =
        doseEventDao.getNextScheduledEvents(fromTime).map { events ->
            events.map { it.toDomain() }
        }

    override fun getDoseEventsByStatus(status: DoseStatus): Flow<List<DoseEvent>> =
        doseEventDao.getEventsByStatus(status.name).map { events ->
            events.map { it.toDomain() }
        }

    override fun getDoseEventsByMedicationAndStatus(
        medicationId: Int,
        status: DoseStatus
    ): Flow<List<DoseEvent>> =
        doseEventDao.getEventsByMedicationAndStatus(medicationId, status.name).map { events ->
            events.map { it.toDomain() }
        }

    override suspend fun markDoseAsTaken(doseEventId: Int, takenAt: Long): Result<Unit> =
        try {
            doseEventDao.markAsTaken(doseEventId, takenAt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun markDoseAsMissed(doseEventId: Int): Result<Unit> =
        try {
            doseEventDao.updateStatus(doseEventId, DoseStatus.MISSED.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun updateDoseStatus(doseEventId: Int, status: DoseStatus): Result<Unit> =
        try {
            doseEventDao.updateStatus(doseEventId, status.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun markOverdueAssMissed(cutoffTime: Long): Result<Unit> =
        try {
            doseEventDao.markOverdueAssMissed(cutoffTime)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun deleteUnresolvedEventsForMedication(medicationId: Int) {
        doseEventDao.deleteUnresolvedEventsForMedication(medicationId)
    }

    override fun countTakenEventsForDay(startOfDay: Long, endOfDay: Long): Flow<Int> =
        doseEventDao.countEventsForDayWithStatus(startOfDay, endOfDay, DoseStatus.TAKEN.name)

    override fun countMissedEventsForDay(startOfDay: Long, endOfDay: Long): Flow<Int> =
        doseEventDao.countEventsForDayWithStatus(startOfDay, endOfDay, DoseStatus.MISSED.name)

    override suspend fun getLastTakenEventForMedication(medicationId: Int): DoseEvent? =
        doseEventDao.getLastTakenEvent(medicationId)?.toDomain()

    override suspend fun generateFutureEvents(medicationId: Int, daysAhead: Int): Result<Int> {
        return try {
            val medication = medicationDao.getMedicationById(medicationId)
                ?: return Result.failure(Exception("Medication not found"))

            val days = DayOfWeek.fromCodes(medication.days)
            if (days.isEmpty()) {
                return Result.failure(Exception("Medication has no scheduled days"))
            }

            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply { timeInMillis = now }
            var eventsCreated = 0

            repeat(daysAhead) {
                val dayOfWeek = DayOfWeek.values()
                    .find { it.calendarValue == calendar.get(Calendar.DAY_OF_WEEK) }

                if (dayOfWeek != null && days.contains(dayOfWeek)) {
                    val scheduledTime = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, medication.timeHour)
                        set(Calendar.MINUTE, medication.timeMinute)
                        set(Calendar.SECOND, 0)
                    }.timeInMillis

                    if (scheduledTime >= now) {
                        createDoseEvent(medicationId, medication.name, scheduledTime)
                        eventsCreated++
                    }
                }

                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            Result.success(eventsCreated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Mappers ─────────────────────────────────────────────

    private fun DoseEventEntity.toDomain() = DoseEvent(
        id = id,
        medicationId = medicationId,
        medicationName = medicationName,
        scheduledTime = scheduledTime,
        status = DoseStatus.valueOf(status),
        takenAt = takenAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun DoseEvent.toEntity() = DoseEventEntity(
        id = id,
        medicationId = medicationId,
        medicationName = medicationName,
        scheduledTime = scheduledTime,
        status = status.name,
        takenAt = takenAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
