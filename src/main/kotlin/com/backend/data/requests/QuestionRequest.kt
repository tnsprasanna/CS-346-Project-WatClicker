package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class QuestionRequest (
    val question: String,
    val options: List<String>,
    val responses: List<Int>,
    val answer: Int,
)
