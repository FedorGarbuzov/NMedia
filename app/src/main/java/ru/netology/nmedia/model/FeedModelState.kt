package ru.netology.nmedia.model

data class FeedModelState(
    val loading: Boolean = false,
    val errorLoading: Boolean = false,
    val errorSaving: Boolean = false,
    val refreshing: Boolean = false,
)
