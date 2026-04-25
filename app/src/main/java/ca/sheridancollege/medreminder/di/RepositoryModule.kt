package ca.sheridancollege.medreminder.di

import ca.sheridancollege.medreminder.data.repository.AuthRepository
import ca.sheridancollege.medreminder.data.repository.AuthRepositoryImpl
import ca.sheridancollege.medreminder.data.repository.DoseEventRepository
import ca.sheridancollege.medreminder.data.repository.DoseEventRepositoryImpl
import ca.sheridancollege.medreminder.data.repository.DrugRepository
import ca.sheridancollege.medreminder.data.repository.DrugRepositoryImpl
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.data.repository.MedicationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMedicationRepository(
        impl: MedicationRepositoryImpl
    ): MedicationRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDrugRepository(
        impl: DrugRepositoryImpl
    ): DrugRepository

    @Binds
    @Singleton
    abstract fun bindDoseEventRepository(
        impl: DoseEventRepositoryImpl
    ): DoseEventRepository
}
