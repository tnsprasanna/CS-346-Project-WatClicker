package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class QuestionListResponse(
    val questions: List<QuestionResponse>
)
