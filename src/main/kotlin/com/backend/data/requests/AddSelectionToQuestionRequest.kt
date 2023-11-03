package com.backend.data.requests

import Selection
import kotlinx.serialization.Serializable

@Serializable
data class AddSelectionToQuestionRequest (
    val questionId: String,
    val selectionId: String
)