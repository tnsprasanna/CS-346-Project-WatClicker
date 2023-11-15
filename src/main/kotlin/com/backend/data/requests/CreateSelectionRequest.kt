package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateSelectionRequest (
    val questionId: String,
    val selectedOption: Int,
)