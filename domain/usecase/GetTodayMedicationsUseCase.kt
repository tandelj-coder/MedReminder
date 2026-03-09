package ca.sheridancollege.medreminder.domain.usecase

import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.domain.model.Medication
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class GetTodayMedicationsUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke(): Flow<List<Medication>> {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todayCode = DayOfWeek.values()
            .find { it.calendarValue == today }?.code ?: "MON"
        return repository.getMedicationsForDay(todayCode)
    }
}