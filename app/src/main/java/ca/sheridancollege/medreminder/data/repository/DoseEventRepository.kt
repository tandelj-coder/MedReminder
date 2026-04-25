package ca.sheridancollege.medreminder.data.repository

import ca.sheridancollege.medreminder.domain.model.DoseEvent
import ca.sheridancollege.medreminder.domain.model.DoseStatus
import kotlinx.coroutines.flow.Flow

interface DoseEventRepository {

    suspend fun createDoseEvent(
        medicationId: Int,
        medicationName: String,
        scheduledTime: Long
    ): Long

    suspend fun getDoseEventById(id: Int): DoseEvent?

    fun getDoseEventsForDay(startOfDay: Long, endOfDay: Long): Flow<List<DoseEvent>>

    fun getDoseEventsForMedicationOnDay(
        medicationId: Int,
        startOfDay: Long,
        endOfDay: Long
    ): Flow<List<DoseEvent>>

    fun getNextScheduledEvents(fromTime: Long): Flow<List<DoseEvent>>

    fun getDoseEventsByStatus(status: DoseStatus): Flow<List<DoseEvent>>

    fun getDoseEventsByMedicationAndStatus(medicationId: Int, status: DoseStatus): Flow<List<DoseEvent>>

    suspend fun markDoseAsTaken(doseEventId: Int, takenAt: Long): Result<Unit>

    suspend fun markDoseAsMissed(doseEventId: Int): Result<Unit>

    suspend fun updateDoseStatus(doseEventId: Int, status: DoseStatus): Result<Unit>

    suspend fun markOverdueAssMissed(cutoffTime: Long): Result<Unit>

    suspend fun deleteUnresolvedEventsForMedication(medicationId: Int)

    fun countTakenEventsForDay(startOfDay: Long, endOfDay: Long): Flow<Int>

    fun countMissedEventsForDay(startOfDay: Long, endOfDay: Long): Flow<Int>

    suspend fun getLastTakenEventForMedication(medicationId: Int): DoseEvent?

    suspend fun generateFutureEvents(medicationId: Int, daysAhead: Int): Result<Int>
}
