package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class QuizListResponse(
    val users: List<QuizResponse>
)
