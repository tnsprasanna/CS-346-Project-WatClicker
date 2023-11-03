package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class EditSelectionRequest (
    val selectionId: String,
    val questionId: String,
    val newOption: Int,
)