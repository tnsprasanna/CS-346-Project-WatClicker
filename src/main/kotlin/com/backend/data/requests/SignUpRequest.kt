package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest (
    val username: String,
    val password: String,
    val role: String,
    val firstname: String,
    val lastname: String
)