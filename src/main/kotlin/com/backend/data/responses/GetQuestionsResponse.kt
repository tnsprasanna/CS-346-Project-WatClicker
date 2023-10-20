package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
class GetQuestionsResponse (
    val questions: Array<String>
)