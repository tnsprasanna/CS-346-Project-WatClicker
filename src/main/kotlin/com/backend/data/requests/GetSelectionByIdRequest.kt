package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class GetSelectionByIdRequest(
    val selectionId: String
)
