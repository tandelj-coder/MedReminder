package ca.sheridancollege.medreminder.data.local.dao

import androidx.room.*
import ca.sheridancollege.medreminder.data.local.entity.IntakeLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeLogDao {

    @Query("SELECT * FROM intake_logs ORDER BY takenAt DESC")
    fun getAllLogs(): Flow<List<IntakeLogEntity>>

    @Query("""
        SELECT * FROM intake_logs 
        WHERE takenAt >= :startOfDay AND takenAt <= :endOfDay
        ORDER BY takenAt DESC
    """)
    fun getLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<IntakeLogEntity>>

    @Query("""
        SELECT * FROM intake_logs 
        WHERE medicationId = :medicationId 
        ORDER BY takenAt DESC LIMIT 30
    """)
    fun getLogsForMedication(medicationId: Int): Flow<List<IntakeLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: IntakeLogEntity)

    @Query("DELETE FROM intake_logs WHERE medicationId = :medicationId")
    suspend fun deleteLogsForMedication(medicationId: Int)

    @Query("""
        SELECT * FROM intake_logs
        WHERE medicationId = :medicationId
        ORDER BY takenAt DESC LIMIT 1
    """)
    suspend fun getLastIntakeForMedication(medicationId: Int): IntakeLogEntity?

    @Query("""
        SELECT COUNT(*) FROM intake_logs 
        WHERE takenAt >= :startOfDay AND takenAt <= :endOfDay
    """)
    suspend fun getCountForDay(startOfDay: Long, endOfDay: Long): Int
}