package com.backend.data.responses

import Quiz
import kotlinx.serialization.Serializable

@Serializable
class GetQuizResponse (
    val questions: Array<String>
)