package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class ChangeStateRequest (
    val quizId: String,
    val newState: String
)