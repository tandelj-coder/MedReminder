package ca.sheridancollege.medreminder.domain.usecase

import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.IntakeLog
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckDoubleDoseUseCaseTest {

    private lateinit var repository: MedicationRepository
    private lateinit var useCase: CheckDoubleDoseUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = CheckDoubleDoseUseCase(repository)
    }

    @Test
    fun `returns risk true when taken 90 minutes ago`() = runTest {
        coEvery { repository.getLastIntakeForMedication(1) } returns IntakeLog(
            id = 1, medicationId = 1, medicationName = "Vitamin D",
            takenAt = System.currentTimeMillis() - (90 * 60 * 1000),
            scheduledHour = 8, scheduledMinute = 0, wasOnTime = true
        )

        val result = useCase(1)

        assertTrue(result.isDoubleDoseRisk)
    }

    @Test
    fun `returns risk false when taken 3 hours ago`() = runTest {
        coEvery { repository.getLastIntakeForMedication(1) } returns IntakeLog(
            id = 1, medicationId = 1, medicationName = "Vitamin D",
            takenAt = System.currentTimeMillis() - (3 * 60 * 60 * 1000),
            scheduledHour = 8, scheduledMinute = 0, wasOnTime = true
        )

        val result = useCase(1)

        assertFalse(result.isDoubleDoseRisk)
    }

    @Test
    fun `returns no risk when no previous intake`() = runTest {
        coEvery { repository.getLastIntakeForMedication(1) } returns null

        val result = useCase(1)

        assertFalse(result.isDoubleDoseRisk)
        assertTrue(result.lastTakenAt == null)
    }

    @Test
    fun `minutesSinceLastDose is accurate`() = runTest {
        coEvery { repository.getLastIntakeForMedication(1) } returns IntakeLog(
            id = 1, medicationId = 1, medicationName = "Vitamin D",
            takenAt = System.currentTimeMillis() - (45 * 60 * 1000),
            scheduledHour = 8, scheduledMinute = 0, wasOnTime = true
        )

        val result = useCase(1)

        assertTrue(result.minutesSinceLastDose in 44..46)
    }
}
