package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class SelectionListResponse(
    val selections: List<SelectionResponse>
)
