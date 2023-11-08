package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class QuestionIdRequest(
    val questionId: String
)
