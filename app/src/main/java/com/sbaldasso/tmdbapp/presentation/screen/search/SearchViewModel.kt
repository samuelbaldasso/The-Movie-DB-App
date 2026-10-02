package com.sbaldasso.tmdbapp.presentation.screen.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sbaldasso.tmdbapp.domain.model.Movie
import com.sbaldasso.tmdbapp.domain.usecase.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMoviesUseCase: SearchMoviesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val query = savedStateHandle.getStateFlow("query", "")

    val searchResults: Flow<PagingData<Movie>> = query
        .map { it.trim() }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length < 3) flowOf(PagingData.empty<Movie>(
                sourceLoadStates = LoadStates(
                    LoadState.NotLoading(true), LoadState.NotLoading(true), LoadState.NotLoading(true)
                )
            ))
            else flow {
                emit(PagingData.empty<Movie>(sourceLoadStates = LoadStates(
                    LoadState.Loading, LoadState.NotLoading(true), LoadState.NotLoading(true)
                )))
                kotlinx.coroutines.delay(500)
                emitAll(searchMoviesUseCase(query))
            }
        }
        .cachedIn(viewModelScope)

    fun onQueryChange(newQuery: String) {
        savedStateHandle["query"] = newQuery
    }
}
