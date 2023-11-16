package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class EditSelectionRequest (
    val selectionId: String,
    val newOption: Int,
)