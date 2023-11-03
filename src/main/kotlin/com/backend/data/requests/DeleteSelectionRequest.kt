package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class DeleteSelectionRequest (
    val selectionId: String,
)