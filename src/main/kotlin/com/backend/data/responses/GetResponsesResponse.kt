package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
class GetResponsesResponse (
    val response: MutableList<Int>
)