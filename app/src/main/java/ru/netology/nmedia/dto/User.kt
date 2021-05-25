package ru.netology.nmedia.dto

data class User(
        var id: Long,
        var login: String,
        var pass: String,
        var name: String,
        var avatar: String,
)

data class Token(
        val id: Long,
        val token: String,
)