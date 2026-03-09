package ca.sheridancollege.medreminder.domain.usecase

import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import javax.inject.Inject

data class DoubleDoseCheck(
    val isDoubleDoseRisk: Boolean,
    val minutesSinceLastDose: Long,
    val lastTakenAt: Long?
)

class CheckDoubleDoseUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medicationId: Int): DoubleDoseCheck {
        val lastLog = repository.getLastIntakeForMedication(medicationId)
            ?: return DoubleDoseCheck(false, 0, null)

        val minutesSince = (System.currentTimeMillis() - lastLog.takenAt) / 60000
        return DoubleDoseCheck(
            isDoubleDoseRisk = minutesSince < 120,
            minutesSinceLastDose = minutesSince,
            lastTakenAt = lastLog.takenAt
        )
    }
}