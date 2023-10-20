package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class QuizRequest (
    val name: String,
    // must be one of HIDDEN | OPEN | CLOSED | FINISHED
    val state: String,
    // must be a list of IDs of existing questions
    val questions: Array<String>
)