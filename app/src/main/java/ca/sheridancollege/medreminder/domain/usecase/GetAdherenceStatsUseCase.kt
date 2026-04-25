package ca.sheridancollege.medreminder.domain.usecase

import ca.sheridancollege.medreminder.data.datastore.UserPreferencesDataStore
import ca.sheridancollege.medreminder.data.repository.DoseEventRepository
import ca.sheridancollege.medreminder.domain.model.AdherenceStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

class GetAdherenceStatsUseCase @Inject constructor(
    private val doseEventRepository: DoseEventRepository,
    private val dataStore: UserPreferencesDataStore
) {
    operator fun invoke(): Flow<AdherenceStats> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = start + 24 * 60 * 60 * 1000L - 1

        return combine(
            doseEventRepository.countTakenEventsForDay(start, end),
            doseEventRepository.getDoseEventsForDay(start, end),
            dataStore.streakCount
        ) { taken: Int, todayEvents, streak: Int ->
            AdherenceStats(
                totalScheduled = todayEvents.size,
                totalTaken = taken,
                currentStreak = streak
            )
        }
    }
}
