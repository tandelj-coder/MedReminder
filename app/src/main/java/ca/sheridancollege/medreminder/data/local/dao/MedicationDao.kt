package ca.sheridancollege.medreminder.data.local.dao

import androidx.room.*
import ca.sheridancollege.medreminder.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY timeHour, timeMinute")
    fun getAllActiveMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Int): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Delete
    suspend fun delete(medication: MedicationEntity)

    @Query("SELECT * FROM medications WHERE isActive = 1 AND days LIKE '%' || :dayCode || '%' ORDER BY timeHour, timeMinute")
    fun getMedicationsForDay(dayCode: String): Flow<List<MedicationEntity>>

    @Query("SELECT COUNT(*) FROM medications WHERE isActive = 1")
    fun getTotalActiveCount(): Flow<Int>
    @Query("SELECT * FROM medications WHERE isSynced = 0")
    suspend fun getUnsyncedMedications(): List<MedicationEntity>

    @Query("SELECT * FROM medications")
    suspend fun getAllMedicationsSync(): List<MedicationEntity>

    @Transaction
    suspend fun upsertFromSync(medications: List<MedicationEntity>) {
        medications.forEach { remote ->
            val local = getMedicationById(remote.id)
            if (local == null || remote.updatedAt > local.updatedAt) {
                insert(remote.copy(isSynced = true))
            }
        }
    }
}
