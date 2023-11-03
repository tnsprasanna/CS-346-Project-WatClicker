package com.backend.data.requests
import kotlinx.serialization.Serializable

@Serializable
class DeleteLectureRequest (
    val lectureId: String
)
