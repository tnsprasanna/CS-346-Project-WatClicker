package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class GetSelectionByUserAndQuestionIdRequest (
    val questionId: String
)