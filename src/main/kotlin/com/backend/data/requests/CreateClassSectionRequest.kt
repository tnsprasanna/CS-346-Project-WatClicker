package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateClassSectionRequest(
    val name: String
)