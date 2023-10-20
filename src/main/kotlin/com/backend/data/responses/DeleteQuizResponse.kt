package com.backend.data.responses

import Quiz
import kotlinx.serialization.Serializable

@Serializable
data class DeleteQuizResponse (
    val deletedQuiz: String
)