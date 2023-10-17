package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequests (
    val username: String,
    val password: String
)