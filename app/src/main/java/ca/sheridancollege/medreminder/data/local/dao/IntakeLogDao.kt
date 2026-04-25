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
        SELECT COUNT(*) FROM intake_logs
        WHERE takenAt >= :startOfDay AND takenAt <= :endOfDay
    """)
    fun getTakenCountForDay(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("""
        SELECT * FROM intake_logs
        WHERE medicationId = :medicationId
        ORDER BY takenAt DESC LIMIT 30
    """)
    fun getLogsForMedication(medicationId: Int): Flow<List<IntakeLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: IntakeLogEntity)

    @Update
    suspend fun update(log: IntakeLogEntity)

    @Query("DELETE FROM intake_logs WHERE medicationId = :medicationId")
    suspend fun deleteLogsForMedication(medicationId: Int)

    @Query("DELETE FROM intake_logs WHERE takenAt >= :startOfDay AND takenAt <= :endOfDay")
    suspend fun deleteLogsForDay(startOfDay: Long, endOfDay: Long)

    @Query("""
        SELECT * FROM intake_logs
        WHERE medicationId = :medicationId
        ORDER BY takenAt DESC LIMIT 1
    """)
    suspend fun getLastIntakeForMedication(medicationId: Int): IntakeLogEntity?

    @Query("SELECT COUNT(*) FROM intake_logs WHERE takenAt >= :startOfDay AND takenAt <= :endOfDay")
    suspend fun getCountForDay(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT * FROM intake_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<IntakeLogEntity>

    @Transaction
    suspend fun upsertFromSync(logs: List<IntakeLogEntity>) {
        logs.forEach { remote ->
            val local = getLastIntakeForMedication(remote.medicationId) // Simple check for now, ideally we'd have a remote ID or check by timestamp
            // Since we don't have a unique remote ID for logs in Room yet (only auto-gen local ID), 
            // we might need a better way to match them. 
            // For now, let's just insert if not present or handle by timestamp/medId.
            // A better way is to add a firestoreId field.
            insert(remote.copy(isSynced = true))
        }
    }
}
