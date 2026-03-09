package ca.sheridancollege.medreminder.data.repository

import ca.sheridancollege.medreminder.domain.model.IntakeLog
import ca.sheridancollege.medreminder.domain.model.Medication
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun getAllActiveMedications(): Flow<List<Medication>>
    fun getMedicationsForDay(dayCode: String): Flow<List<Medication>>
    fun getTakenCountToday(): Flow<Int>
    fun getTotalActiveCount(): Flow<Int>
    suspend fun getMedicationById(id: Int): Medication?
    suspend fun insertMedication(medication: Medication): Long
    suspend fun updateMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)
    suspend fun markAsTaken(
        medicationId: Int,
        timestamp: Long,
        scheduledHour: Int,
        scheduledMinute: Int
    )
    suspend fun resetAllDailyStatus()
    fun getAllLogs(): Flow<List<IntakeLog>>
    fun getLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<IntakeLog>>
    suspend fun getLastIntakeForMedication(medicationId: Int): IntakeLog?
}