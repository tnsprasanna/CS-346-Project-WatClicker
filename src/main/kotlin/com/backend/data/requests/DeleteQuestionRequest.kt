package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class DeleteQuestionRequest(
    val quizId: String,
    val questionId: String
)
