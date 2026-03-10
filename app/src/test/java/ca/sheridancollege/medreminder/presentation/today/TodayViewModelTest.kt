package ca.sheridancollege.medreminder.presentation.today

import app.cash.turbine.test
import ca.sheridancollege.medreminder.domain.model.AdherenceStats
import ca.sheridancollege.medreminder.domain.model.DayOfWeek
import ca.sheridancollege.medreminder.domain.model.Medication
import ca.sheridancollege.medreminder.domain.usecase.GetAdherenceStatsUseCase
import ca.sheridancollege.medreminder.domain.usecase.GetTodayMedicationsUseCase
import ca.sheridancollege.medreminder.domain.usecase.MarkAsTakenResult
import ca.sheridancollege.medreminder.domain.usecase.MarkAsTakenUseCase
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

    private lateinit var getTodayMedications: GetTodayMedicationsUseCase
    private lateinit var markAsTaken: MarkAsTakenUseCase
    private lateinit var getAdherenceStats: GetAdherenceStatsUseCase
    private lateinit var viewModel: TodayViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val fakeMeds = listOf(
        Medication(id = 1, name = "Vitamin D", dosage = "1000 IU",
            timeHour = 8, timeMinute = 0, days = listOf(DayOfWeek.MON)),
        Medication(id = 2, name = "Omega 3", dosage = "500mg",
            timeHour = 12, timeMinute = 0, days = listOf(DayOfWeek.MON))
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTodayMedications = mockk()
        markAsTaken = mockk()
        getAdherenceStats = mockk()
        every { getTodayMedications() } returns flowOf(fakeMeds)
        every { getAdherenceStats() } returns flowOf(AdherenceStats(2, 0, 3))
        viewModel = TodayViewModel(getTodayMedications, markAsTaken, getAdherenceStats)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState loads medications correctly`() = runTest {
        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertEquals(2, state.medications.size)
            assertEquals("Vitamin D", state.medications[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState shows snackbar on success`() = runTest {
        coEvery { markAsTaken(fakeMeds[0], false) } returns MarkAsTakenResult.Success

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // loaded state

            viewModel.onMarkAsTaken(fakeMeds[0])
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertNotNull(state.snackbarMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState shows double dose warning`() = runTest {
        coEvery { markAsTaken(fakeMeds[0], false) } returns MarkAsTakenResult.DoubleDoseWarning(
            medication = fakeMeds[0],
            lastTakenAt = System.currentTimeMillis() - (60 * 60 * 1000),
            minutesSinceLastDose = 60
        )

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()

            viewModel.onMarkAsTaken(fakeMeds[0])
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertNotNull(state.doubleDoseWarning)
            assertEquals("Vitamin D", state.doubleDoseWarning!!.medication.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissDoubleDoseWarning clears warning state`() = runTest {
        coEvery { markAsTaken(fakeMeds[0], false) } returns MarkAsTakenResult.DoubleDoseWarning(
            medication = fakeMeds[0],
            lastTakenAt = System.currentTimeMillis(),
            minutesSinceLastDose = 30
        )

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()

            viewModel.onMarkAsTaken(fakeMeds[0])
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
