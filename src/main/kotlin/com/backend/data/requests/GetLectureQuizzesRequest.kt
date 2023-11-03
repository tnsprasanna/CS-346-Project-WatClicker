package com.backend.data.requests
import kotlinx.serialization.Serializable

@Serializable
class GetLectureQuizzesRequest (
    val lectureId: String
)