package ca.sheridancollege.medreminder.domain.usecase

import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import javax.inject.Inject

class ResetDailyStatusUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke() {
        repository.resetAllDailyStatus()
    }
}