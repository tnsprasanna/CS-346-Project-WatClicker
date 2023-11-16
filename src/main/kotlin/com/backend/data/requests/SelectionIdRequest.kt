package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class SelectionIdRequest(
    val selectionId: String
)
