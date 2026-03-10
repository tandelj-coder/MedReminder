package ca.sheridancollege.medreminder.domain.usecase

import ca.sheridancollege.medreminder.data.datastore.UserPreferencesDataStore
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.AdherenceStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetAdherenceStatsUseCase @Inject constructor(
    private val repository: MedicationRepository,
    private val dataStore: UserPreferencesDataStore
) {
    operator fun invoke(): Flow<AdherenceStats> {
        return combine(
            repository.getTakenCountToday(),
            repository.getTotalActiveCount(),
            dataStore.streakCount
        ) { taken: Int, total: Int, streak: Int ->
            AdherenceStats(
                totalScheduled = total,
                totalTaken = taken,
                currentStreak = streak
            )
        }
    }
}
