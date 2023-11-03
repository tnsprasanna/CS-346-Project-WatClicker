package com.backend.data.responses
import kotlinx.serialization.Serializable

@Serializable
class GetLectureQuizzesResponse (
    val quizIds: MutableList<String>,
)