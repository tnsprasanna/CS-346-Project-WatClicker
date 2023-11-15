package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class RemoveStudentFromClassSectionRequest(
    val classSectionId: String,
    val userid: String
)
