package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class JoinClassSectionRequest (
    val userId: String,
    val classSectionId: String,
    val classSectionJoinCode: String
)