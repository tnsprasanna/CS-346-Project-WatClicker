package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class ChangeUsernameRequest(
    val userId: String,
    val newUsername: String,
)
