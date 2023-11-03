package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class ClassSectionListResponse(
    val classSections: List<ClassSectionResponse>
)