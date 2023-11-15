package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class AddSelectionToQuestionRequest (
    val questionId: String,
    val selectionId: String
)