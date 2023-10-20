package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class QuestionRequest (
    val question: String,
    val options: Array<String>,
    val responses: Array<Int>,
    val answer: Int,
)
