import androidx.paging.PagingData
import app.cash.turbine.test
import com.sbaldasso.tmdbapp.domain.model.Movie
import com.sbaldasso.tmdbapp.domain.usecase.GetPopularMoviesUseCase
import com.sbaldasso.tmdbapp.presentation.screen.home.HomeViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getPopularMoviesUseCase: GetPopularMoviesUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPopularMoviesUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `moviesFlow should emit paging data from use case`() = runTest {
        // Given
        val pagingData = PagingData.from(emptyList<Movie>())
        every { getPopularMoviesUseCase() } returns flowOf(pagingData)

        // When
        viewModel = HomeViewModel(getPopularMoviesUseCase)

        // Then
        viewModel.moviesFlow.test {
            assertNotNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}