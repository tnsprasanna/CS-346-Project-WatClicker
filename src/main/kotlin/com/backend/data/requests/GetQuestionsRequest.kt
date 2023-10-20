package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class GetQuestionsRequest (
    val quizId: String

)