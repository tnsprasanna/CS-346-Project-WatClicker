package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class ChangeUsernameRequest(
    val newUsername: String,
)
