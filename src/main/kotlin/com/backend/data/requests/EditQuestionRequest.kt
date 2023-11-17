package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class EditQuestionRequest (
    val questionId: String,
    val question: String,
    val options: List<String>,
    val answer: Int,
)
