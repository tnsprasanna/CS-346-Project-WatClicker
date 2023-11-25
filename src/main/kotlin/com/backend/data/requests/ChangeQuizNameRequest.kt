package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class ChangeQuizNameRequest(
    val quizId: String,
    val newName: String
)
