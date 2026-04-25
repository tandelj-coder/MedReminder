package ca.sheridancollege.medreminder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ca.sheridancollege.medreminder.data.local.dao.IntakeLogDao
import ca.sheridancollege.medreminder.data.local.dao.MedicationDao
import ca.sheridancollege.medreminder.data.local.entity.IntakeLogEntity
import ca.sheridancollege.medreminder.data.local.entity.MedicationEntity

@Database(
    entities = [MedicationEntity::class, IntakeLogEntity::class],
    version = 4,
    exportSchema = false
)
abstract class MedicationDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun intakeLogDao(): IntakeLogDao
}
