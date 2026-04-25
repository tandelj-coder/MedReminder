package ca.sheridancollege.medreminder.domain.usecase

import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.DoseEvent
import ca.sheridancollege.medreminder.domain.model.DoseStatus
import ca.sheridancollege.medreminder.domain.model.IntakeLog
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

    private val now = System.currentTimeMillis()
    private val doseEvent = DoseEvent(
        id             = 10,
        medicationId   = 1,
        medicationName = "Vitamin D",
        scheduledTime  = now,
        status         = DoseStatus.SCHEDULED,
        createdAt      = now,
        updatedAt      = now
    )

    @Before
    fun setup() {
        repository = mockk()
    }

    @Test
    fun `returns Success when no recent intake`() = runTest {
        useCase = MarkAsTakenUseCase(repository)
        coEvery { repository.getLastIntakeForMedication(1) } returns null
        coEvery { repository.markAsTaken(any(), any(), any(), any(), any()) } returns Unit

        val result = useCase(doseEvent)

        assertTrue(result is MarkAsTakenResult.Success)
        coVerify {
            repository.markAsTaken(
                medicationId    = 1,
                timestamp       = any(),
                scheduledHour   = any(),
                scheduledMinute = any(),
                doseEventId     = 10
            )
        }
    }

    @Test
    fun `returns DoubleDoseWarning when taken within 2 hours`() = runTest {
        useCase = MarkAsTakenUseCase(repository)
        val recentLog = IntakeLog(
            id                   = 1,
            medicationId         = 1,
            medicationName       = "Vitamin D",
            takenAt              = now - (60 * 60 * 1000),
            scheduledHour        = 8,
            scheduledMinute      = 0,
            wasOnTime            = true
        )
        coEvery { repository.getLastIntakeForMedication(1) } returns recentLog

        val result = useCase(doseEvent)

        assertTrue(result is MarkAsTakenResult.DoubleDoseWarning)
        coVerify(exactly = 0) { repository.markAsTaken(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `forceConfirm bypasses double dose warning`() = runTest {
        useCase = MarkAsTakenUseCase(repository)
        val recentLog = IntakeLog(
            id                   = 1,
            medicationId         = 1,
            medicationName       = "Vitamin D",
            takenAt              = now - (60 * 60 * 1000),
            scheduledHour        = 8,
            scheduledMinute      = 0,
            wasOnTime            = true
        )
        coEvery { repository.getLastIntakeForMedication(1) } returns recentLog
        coEvery { repository.markAsTaken(any(), any(), any(), any(), any()) } returns Unit

        val result = useCase(doseEvent, forceConfirm = true)

        assertTrue(result is MarkAsTakenResult.Success)
    }
}
