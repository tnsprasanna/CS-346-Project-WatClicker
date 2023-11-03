package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
class GetQuizzesResponse (
    val quizIds: List<String>
)