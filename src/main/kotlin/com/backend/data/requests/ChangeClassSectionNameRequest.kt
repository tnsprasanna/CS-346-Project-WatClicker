package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class ChangeClassSectionNameRequest(
    val classSectionId: String,
    val newName: String
)
