package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class QuizResponse(
    val id: String,
    val name: String,
    val state: String,
    val classSectionId: String,
    val questionIds: List<String>,
)
