package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class GetQuizRequest (
    val quizId: String
)