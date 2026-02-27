package com.example.kotlinproject

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.kotlinproject.Model.User
import com.example.kotlinproject.Repo.UserRepo
import com.example.kotlinproject.ViewModel.AuthViewModel
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
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var mockRepo: UserRepo

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(mockRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login should update authState to Success when repository returns success`() {
        runTest {
            // Given
            val email = "test@example.com"
            val password = "password123"
            val expectedUser = User(
                uid = "user_123",
                fullName = "Test User",
                email = email,
                role = "patient"
            )

            doAnswer { invocation: InvocationOnMock ->
                val callback = invocation.getArgument<(Boolean, String, User?) -> Unit>(2)
                callback.invoke(true, "Login Successful", expectedUser)
                null
            }.whenever(mockRepo).login(eq(email), eq(password), any())

            // When
            viewModel.login(email, password)

            // Then
            verify(mockRepo).login(eq(email), eq(password), any())
            val state = viewModel.authState.value
            assert(state is AuthViewModel.AuthState.Success)
            if (state is AuthViewModel.AuthState.Success) {
                assert(state.user == expectedUser)
            }
            assert(viewModel.currentUser.value == expectedUser)
        }
    }

    @Test
    fun `logout should clear currentUser and set authState to Idle`() {
        runTest {
            // When
            viewModel.logout()

            // Then
            verify(mockRepo).logout()
            assert(viewModel.currentUser.value == null)
            assert(viewModel.authState.value is AuthViewModel.AuthState.Idle)
        }
    }

    @Test
    fun `login should update authState to Error when repository returns failure`() {
        runTest {
            // Given
            val email = "wrong@example.com"
            val password = "wrongpassword"
            val errorMessage = "Invalid credentials"

            doAnswer { invocation: InvocationOnMock ->
                val callback = invocation.getArgument<(Boolean, String, User?) -> Unit>(2)
                callback.invoke(false, errorMessage, null)
                null
            }.whenever(mockRepo).login(eq(email), eq(password), any())

            // When
            viewModel.login(email, password)

            // Then
            val state = viewModel.authState.value
            assert(state is AuthViewModel.AuthState.Error)
            if (state is AuthViewModel.AuthState.Error) {
                assert(state.message == errorMessage)
            }
            assert(viewModel.currentUser.value == null)
        }
    }

    @Test
    fun `register should update authState to Success when repository returns success`() {
        runTest {
            // Given
            val name = "New User"
            val email = "new@example.com"
            val pass = "pass123"
            val role = "patient"
            val expectedUser = User(uid = "uid_new", fullName = name, email = email, role = role)

            doAnswer { invocation: InvocationOnMock ->
                val callback = invocation.getArgument<(Boolean, String, User?) -> Unit>(4)
                callback.invoke(true, "Registration Successful", expectedUser)
                null
            }.whenever(mockRepo).register(eq(name), eq(email), eq(pass), eq(role), any())

            // When
            viewModel.register(name, email, pass, role)

            // Then
            val state = viewModel.authState.value
            assert(state is AuthViewModel.AuthState.Success)
            if (state is AuthViewModel.AuthState.Success) {
                assert(state.user == expectedUser)
            }
        }
    }

    @Test
    fun `isUserLoggedIn should return false when repository returns true`() {
        // This test is designed to FAIL as requested
        // Given
        whenever(mockRepo.isUserLoggedIn()).thenReturn(true)

        // When
        val result = viewModel.isUserLoggedIn()

        // Then
        // This assertion will fail because result is true, but we are asserting it is false
        assert(result == false)
    }
}
