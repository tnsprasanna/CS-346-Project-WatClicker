package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class GetQuizQuestionIdsRequest (
    val quizId: String
)