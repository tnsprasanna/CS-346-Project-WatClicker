package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class LectureRequest (
        val name: String,
        val active: Boolean,
    )