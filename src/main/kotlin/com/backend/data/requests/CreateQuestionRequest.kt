package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateQuestionRequest (
    val quizId: String,
    val question: String,
    val options: List<String>,
    val answer: Int,
)
