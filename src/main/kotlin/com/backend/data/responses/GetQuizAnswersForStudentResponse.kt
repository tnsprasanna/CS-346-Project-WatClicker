package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class GetQuizAnswersForStudentResponse(
    val id: String,
    val question: String,
    val options: List<String>,
    val responses: List<Int>,
    val answer: Int,
)
