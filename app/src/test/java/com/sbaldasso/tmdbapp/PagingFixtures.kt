package com.sbaldasso.tmdbapp

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData

fun <T : Any> pagingData(items: List<T>): PagingData<T> = PagingData.from(
    items,
    sourceLoadStates = LoadStates(
        LoadState.NotLoading(true), LoadState.NotLoading(true), LoadState.NotLoading(true)
    )
)
