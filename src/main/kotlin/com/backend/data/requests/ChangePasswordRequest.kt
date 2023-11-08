package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
    val newPassword: String
)
