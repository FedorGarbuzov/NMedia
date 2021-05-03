package ru.netology.nmedia.entity

import androidx.room.PrimaryKey

data class UserEntity(
        @PrimaryKey
        var id: Long,
        var login: String,
        var pass: String,
        var name: String,
        var avatar: String,
)