package com.backend.data.requests

import kotlinx.serialization.Serializable
@Serializable
data class UserIdRequest(
    val userId: String
)