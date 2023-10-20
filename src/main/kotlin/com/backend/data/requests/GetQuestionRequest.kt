package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class GetQuestionRequest (
    val questionId: String,
)