package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class ResponseListResponse(
    val responses: List<MutableList<Int>?>
)