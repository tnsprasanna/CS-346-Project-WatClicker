package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class ChangeNameRequest(
    val userId: String,
    val newFirstName: String,
    val newLastName: String
)
