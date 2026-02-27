package com.example.kotlinproject

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.kotlinproject.Repo.AppointmentRepo
import com.example.kotlinproject.ViewModel.PatientViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class PatientViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var mockAppointmentRepo: AppointmentRepo

    private lateinit var viewModel: PatientViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = PatientViewModel(mockAppointmentRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `book appointment should fail due to wrong assertion`() {
        runTest {
            val patientId = "p123"
            val patientName = "John Doe"
            val reason = "Checkup"

            // Mock success response from repository
            doAnswer { invocation: InvocationOnMock ->
                val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
                callback.invoke(true, "Success")
                null
            }.whenever(mockAppointmentRepo).bookAppointment(any(), any())

            viewModel.bookAppointment(patientId, patientName, reason)

            // FAIILURE POINT: We assert that the state is Error, even though it should be Success
            assert(viewModel.bookingState.value is PatientViewModel.BookingState.Error)
        }
    }
}
