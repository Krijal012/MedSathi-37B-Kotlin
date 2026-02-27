package com.example.kotlinproject

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.kotlinproject.ViewModel.StaffViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class StaffViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var mockDb: FirebaseFirestore

    private lateinit var viewModel: StaffViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = StaffViewModel(mockDb)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial stats should be empty`() {
        runTest {
            // Given a new ViewModel with mocked DB
            // When getting stats
            val stats = viewModel.stats.value

            // Then stats should be null or empty initially
            assert(stats == null || stats.isEmpty())
        }
    }
}
