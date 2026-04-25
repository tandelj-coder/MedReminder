package ca.sheridancollege.medreminder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ca.sheridancollege.medreminder.data.local.converter.MedicationConverters
import ca.sheridancollege.medreminder.data.local.dao.DoseEventDao
import ca.sheridancollege.medreminder.data.local.dao.DrugSuggestionDao
import ca.sheridancollege.medreminder.data.local.dao.IntakeLogDao
import ca.sheridancollege.medreminder.data.local.dao.MedicationDao
import ca.sheridancollege.medreminder.data.local.entity.DoseEventEntity
import ca.sheridancollege.medreminder.data.local.entity.DrugSuggestionEntity
import ca.sheridancollege.medreminder.data.local.entity.IntakeLogEntity
import ca.sheridancollege.medreminder.data.local.entity.MedicationEntity

@Database(
    entities = [MedicationEntity::class, IntakeLogEntity::class, DoseEventEntity::class, DrugSuggestionEntity::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(MedicationConverters::class)
abstract class MedicationDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun intakeLogDao(): IntakeLogDao
    abstract fun doseEventDao(): DoseEventDao
    abstract fun drugSuggestionDao(): DrugSuggestionDao
}
