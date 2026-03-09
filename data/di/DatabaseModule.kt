package ca.sheridancollege.medreminder.di

import android.content.Context
import androidx.room.Room
import ca.sheridancollege.medreminder.data.local.MedicationDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MedicationDatabase {
        return Room.databaseBuilder(
            context,
            MedicationDatabase::class.java,
            "medication_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideMedicationDao(db: MedicationDatabase) = db.medicationDao()

    @Provides
    fun provideIntakeLogDao(db: MedicationDatabase) = db.intakeLogDao()
}