package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class QuestionResponse(
    val id: String,
    val question: String,
    val options: List<String>,
    val responses: List<Int>,
    val answer: Int,
    val selections: List<String>
)