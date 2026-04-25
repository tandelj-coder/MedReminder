package ca.sheridancollege.medreminder.presentation.today

import app.cash.turbine.test
import ca.sheridancollege.medreminder.data.repository.DoseEventRepository
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.AdherenceStats
import ca.sheridancollege.medreminder.domain.model.DoseEvent
import ca.sheridancollege.medreminder.domain.model.DoseStatus
import ca.sheridancollege.medreminder.domain.usecase.DeleteMedicationUseCase
import ca.sheridancollege.medreminder.domain.usecase.GetAdherenceStatsUseCase
import ca.sheridancollege.medreminder.domain.usecase.ResetDailyStatusUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    private lateinit var doseEventRepository: DoseEventRepository
    private lateinit var medicationRepository: MedicationRepository
    private lateinit var getAdherenceStats: GetAdherenceStatsUseCase
    private lateinit var deleteMedication: DeleteMedicationUseCase
    private lateinit var resetDailyStatus: ResetDailyStatusUseCase
    private lateinit var viewModel: TodayViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val now = System.currentTimeMillis()

    private val fakeDoseEvents = listOf(
        DoseEvent(id = 1, medicationId = 10, medicationName = "Vitamin D",
            scheduledTime = now + 60_000, status = DoseStatus.SCHEDULED,
            createdAt = now, updatedAt = now),
        DoseEvent(id = 2, medicationId = 20, medicationName = "Omega 3",
            scheduledTime = now + 120_000, status = DoseStatus.SCHEDULED,
            createdAt = now, updatedAt = now)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        doseEventRepository = mockk()
        medicationRepository = mockk()
        getAdherenceStats = mockk()
        deleteMedication = mockk()
        resetDailyStatus = mockk()
        every { doseEventRepository.getDoseEventsForDay(any(), any()) } returns flowOf(fakeDoseEvents)
        every { getAdherenceStats() } returns flowOf(AdherenceStats(2, 0, 3))
        every { medicationRepository.getAllActiveMedications() } returns flowOf(emptyList())
        coEvery { deleteMedication(any()) } returns Unit
        coEvery { resetDailyStatus() } returns Unit
        viewModel = TodayViewModel(
            doseEventRepository, medicationRepository,
            getAdherenceStats, deleteMedication, resetDailyStatus
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState loads dose events correctly`() = runTest {
        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertEquals(2, state.doseEvents.size)
            assertEquals("Vitamin D", state.doseEvents[0].medicationName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onMarkAsTaken shows snackbar on success`() = runTest {
        val dose = fakeDoseEvents[0]
        coEvery { medicationRepository.getLastIntakeForMedication(dose.medicationId) } returns null
        coEvery { medicationRepository.markAsTaken(any(), any(), any(), any(), any()) } returns Unit

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // loaded state

            viewModel.onMarkAsTaken(dose)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertNotNull(state.snackbarMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onMarkAsTaken shows double dose warning within 2 hours`() = runTest {
        val dose = fakeDoseEvents[0]
        val recentLog = mockk<ca.sheridancollege.medreminder.domain.model.IntakeLog>()
        every { recentLog.takenAt } returns System.currentTimeMillis() - (60 * 60 * 1000) // 60 min ago
        coEvery { medicationRepository.getLastIntakeForMedication(dose.medicationId) } returns recentLog

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()

            viewModel.onMarkAsTaken(dose)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertNotNull(state.doubleDoseWarning)
            assertEquals(dose, state.doubleDoseWarning!!.doseEvent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissDoubleDoseWarning clears warning state`() = runTest {
        val dose = fakeDoseEvents[0]
        val recentLog = mockk<ca.sheridancollege.medreminder.domain.model.IntakeLog>()
        every { recentLog.takenAt } returns System.currentTimeMillis() - (30 * 60 * 1000) // 30 min ago
        coEvery { medicationRepository.getLastIntakeForMedication(dose.medicationId) } returns recentLog

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()

            viewModel.onMarkAsTaken(dose)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // warning state

            viewModel.onDismissDoubleDoseWarning()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertNull(state.doubleDoseWarning)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
