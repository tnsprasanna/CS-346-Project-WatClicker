package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class DeleteQuizRequest (
    val quizId: String
)