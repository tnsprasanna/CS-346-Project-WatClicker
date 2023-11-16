package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class JoinClassSectionRequest (
    val classSectionJoinCode: String
)