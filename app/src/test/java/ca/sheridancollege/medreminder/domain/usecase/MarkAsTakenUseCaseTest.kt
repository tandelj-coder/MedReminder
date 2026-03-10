package ca.sheridancollege.medreminder.domain.usecase

import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.domain.model.IntakeLog
import ca.sheridancollege.medreminder.domain.model.Medication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MarkAsTakenUseCaseTest {

    private lateinit var repository: MedicationRepository
    private lateinit var useCase: MarkAsTakenUseCase

    private val medication = Medication(
        id = 1,
        name = "Vitamin D",
        dosage = "1000 IU",
        timeHour = 8,
        timeMinute = 0,
        days = listOf(DayOfWeek.MON),
        isTakenToday = false
    )

    @Before
    fun setup() {
        repository = mockk()
    }

    @Test
    fun `returns Success when not taken today and no recent intake`() = runTest {
        useCase = MarkAsTakenUseCase(repository)
        coEvery { repository.getLastIntakeForMedication(1) } returns null
        coEvery { repository.markAsTaken(any(), any(), any(), any()) } returns Unit

        val result = useCase(medication)

        assertTrue(result is MarkAsTakenResult.Success)
        coVerify { repository.markAsTaken(medicationId = 1, timestamp = any(), scheduledHour = 8, scheduledMinute = 0) }
    }

    @Test
    fun `returns AlreadyTaken when isTakenToday is true`() = runTest {
        useCase = MarkAsTakenUseCase(repository)
        val takenMedication = medication.copy(isTakenToday = true)

        val result = useCase(takenMedication)

        assertTrue(result is MarkAsTakenResult.AlreadyTaken)
        coVerify(exactly = 0) { repository.markAsTaken(any(), any(), any(), any()) }
    }

    @Test
    fun `returns DoubleDoseWarning when taken within 2 hours`() = runTest {
        useCase = MarkAsTakenUseCase(repository)
        val recentLog = IntakeLog(
            id = 1,
            medicationId = 1,
            medicationName = "Vitamin D",
            takenAt = System.currentTimeMillis() - (60 * 60 * 1000), // 1 hour ago
            scheduledHour = 8,
            scheduledMinute = 0,
            wasOnTime = true
        )
        coEvery { repository.getLastIntakeForMedication(1) } returns recentLog

        val result = useCase(medication)

        assertTrue(result is MarkAsTakenResult.DoubleDoseWarning)
        coVerify(exactly = 0) { repository.markAsTaken(any(), any(), any(), any()) }
    }

    @Test
    fun `forceConfirm bypasses double dose warning`() = runTest {
        useCase = MarkAsTakenUseCase(repository)
        val recentLog = IntakeLog(
            id = 1,
            medicationId = 1,
            medicationName = "Vitamin D",
            takenAt = System.currentTimeMillis() - (60 * 60 * 1000),
            scheduledHour = 8,
            scheduledMinute = 0,
            wasOnTime = true
        )
        coEvery { repository.getLastIntakeForMedication(1) } returns recentLog
        coEvery { repository.markAsTaken(any(), any(), any(), any()) } returns Unit

        val result = useCase(medication, forceConfirm = true)

        assertTrue(result is MarkAsTakenResult.Success)
    }
}
