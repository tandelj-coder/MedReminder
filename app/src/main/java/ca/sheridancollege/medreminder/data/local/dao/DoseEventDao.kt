package ca.sheridancollege.medreminder.data.local.dao

import androidx.room.*
import ca.sheridancollege.medreminder.data.local.entity.DoseEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseEventDao {

    @Query("SELECT * FROM dose_events WHERE medicationId = :medicationId AND scheduledTime = :scheduledTime LIMIT 1")
    suspend fun getByMedicationAndScheduledTime(medicationId: Int, scheduledTime: Long): DoseEventEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: DoseEventEntity): Long

    @Update
    suspend fun update(event: DoseEventEntity)

    @Delete
    suspend fun delete(event: DoseEventEntity)

    @Query("SELECT * FROM dose_events WHERE id = :id")
    suspend fun getById(id: Int): DoseEventEntity?

    @Query("""
        SELECT * FROM dose_events
        WHERE scheduledTime >= :startOfDay AND scheduledTime <= :endOfDay
        ORDER BY scheduledTime ASC
    """)
    fun getEventsForDay(startOfDay: Long, endOfDay: Long): Flow<List<DoseEventEntity>>

    @Query("""
        SELECT * FROM dose_events
        WHERE medicationId = :medicationId
        AND scheduledTime >= :startOfDay AND scheduledTime <= :endOfDay
        ORDER BY scheduledTime ASC
    """)
    fun getEventsForMedicationOnDay(medicationId: Int, startOfDay: Long, endOfDay: Long): Flow<List<DoseEventEntity>>

    @Query("""
        SELECT * FROM dose_events
        WHERE scheduledTime >= :startTime AND scheduledTime <= :endTime
        ORDER BY scheduledTime ASC
    """)
    fun getEventsBetween(startTime: Long, endTime: Long): Flow<List<DoseEventEntity>>

    @Query("""
        SELECT * FROM dose_events
        WHERE status = :status
        ORDER BY scheduledTime DESC
    """)
    fun getEventsByStatus(status: String): Flow<List<DoseEventEntity>>

    @Query("""
        SELECT * FROM dose_events
        WHERE medicationId = :medicationId
        AND status = :status
        ORDER BY scheduledTime DESC
    """)
    fun getEventsByMedicationAndStatus(medicationId: Int, status: String): Flow<List<DoseEventEntity>>

    @Query("""
        SELECT * FROM dose_events
        WHERE scheduledTime >= :startTime
        ORDER BY scheduledTime ASC
        LIMIT 7
    """)
    fun getNextScheduledEvents(startTime: Long): Flow<List<DoseEventEntity>>

    @Query("""
        SELECT COUNT(*) FROM dose_events
        WHERE scheduledTime >= :startOfDay AND scheduledTime <= :endOfDay
        AND status = :status
    """)
    fun countEventsForDayWithStatus(startOfDay: Long, endOfDay: Long, status: String): Flow<Int>

    @Query("""
        SELECT * FROM dose_events
        WHERE medicationId = :medicationId
        AND status = 'TAKEN'
        ORDER BY takenAt DESC
        LIMIT 1
    """)
    suspend fun getLastTakenEvent(medicationId: Int): DoseEventEntity?

    @Query("""
        UPDATE dose_events
        SET status = :newStatus, updatedAt = :now, isSynced = 0
        WHERE id = :id
    """)
    suspend fun updateStatus(id: Int, newStatus: String, now: Long = System.currentTimeMillis())

    @Query("""
        UPDATE dose_events
        SET status = :newStatus, takenAt = :takenAt, updatedAt = :now, isSynced = 0
        WHERE id = :id
    """)
    suspend fun markAsTaken(id: Int, takenAt: Long, newStatus: String = "TAKEN", now: Long = System.currentTimeMillis())

    @Query("""
        UPDATE dose_events
        SET status = 'MISSED', updatedAt = :now, isSynced = 0
        WHERE status = 'SCHEDULED' AND scheduledTime <= :cutoffTime
    """)
    suspend fun markOverdueAssMissed(cutoffTime: Long, now: Long = System.currentTimeMillis())

    @Query("""
        DELETE FROM dose_events
        WHERE medicationId = :medicationId AND status NOT IN ('TAKEN', 'MISSED')
    """)
    suspend fun deleteUnresolvedEventsForMedication(medicationId: Int)

    @Query("SELECT * FROM dose_events ORDER BY scheduledTime DESC")
    fun getAllEvents(): Flow<List<DoseEventEntity>>

    @Query("SELECT * FROM dose_events WHERE isSynced = 0")
    suspend fun getUnsyncedEvents(): List<DoseEventEntity>

    @Transaction
    suspend fun upsertFromSync(events: List<DoseEventEntity>) {
        events.forEach { remote ->
            val local = getById(remote.id)
            if (local == null || remote.updatedAt > local.updatedAt) {
                insert(remote.copy(isSynced = true))
            }
        }
    }
}
