package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
class GetResponsesFromQuestionRequest (
    val questionId: String
)