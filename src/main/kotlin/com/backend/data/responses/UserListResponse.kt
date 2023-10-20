package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class UserListResponse (
    val users: List<UserResponse>
)