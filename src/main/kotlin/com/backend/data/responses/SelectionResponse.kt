package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class SelectionResponse(
    val id: String,
    val questionId: String,
    val studentId: String,
    val selectedOption: Int,
    val isCorrect: Boolean
)