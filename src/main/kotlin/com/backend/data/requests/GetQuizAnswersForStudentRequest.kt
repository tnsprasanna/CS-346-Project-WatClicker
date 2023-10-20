package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class GetQuizAnswersForStudentRequest (
    val quizId: String,
    val studentId: String
)