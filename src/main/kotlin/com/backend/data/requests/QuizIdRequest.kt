package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class QuizIdRequest(
    val quizId: String
)
