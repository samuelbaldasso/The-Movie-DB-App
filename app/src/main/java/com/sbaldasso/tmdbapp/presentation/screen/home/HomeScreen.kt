package com.sbaldasso.tmdbapp.presentation.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.sbaldasso.tmdbapp.presentation.component.ErrorView
import com.sbaldasso.tmdbapp.presentation.component.LoadingIndicator
import com.sbaldasso.tmdbapp.presentation.component.MovieCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onMovieClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val movies = viewModel.moviesFlow.collectAsLazyPagingItems()

    val refreshing = movies.loadState.refresh is LoadState.Loading
    val refreshState = rememberPullRefreshState(refreshing, { movies.refresh() })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filmes Populares") },
                actions = {
                    IconButton(onClick = onFavoritesClick) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favoritos")
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(refreshState)
        ) {
            when {
                movies.itemCount == 0 && refreshing -> {
                    LoadingIndicator()
                }

                movies.itemCount == 0 && movies.loadState.refresh is LoadState.Error -> {
                    val error = movies.loadState.refresh as LoadState.Error
                    ErrorView(
                        message = error.error.message ?: "Erro ao carregar filmes",
                        onRetry = { movies.retry() }
                    )
                }

                movies.itemCount == 0 -> {
                    Text("Nenhum filme disponível.", modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(160.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            count = movies.itemCount,
                            key = movies.itemKey { it.id }
                        ) { index ->
                            movies[index]?.let { movie ->
                                MovieCard(
                                    movie = movie,
                                    onClick = { onMovieClick(movie.id) }
                                )
                            }
                        }

                        // Carregamento de mais itens
                        if (movies.loadState.append is LoadState.Loading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                }
                            }
                        }

                        // Erro ao carregar mais itens
                        if (movies.loadState.append is LoadState.Error) {
                            val error = movies.loadState.append as LoadState.Error
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ErrorView(
                                    message = error.error.message ?: "Erro ao carregar mais",
                                    onRetry = { movies.retry() }
                                )
                            }
                        }
                    }
                }
            }
            if (movies.itemCount > 0 && movies.loadState.refresh is LoadState.Error) {
                Surface(modifier = Modifier.align(Alignment.BottomCenter)) {
                    TextButton(onClick = { movies.retry() }) {
                        Text("Sem atualização. Exibindo cache. Tentar novamente")
                    }
                }
            }
            PullRefreshIndicator(refreshing, refreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}
