package com.backend.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class QuestionRequest (
    val question: String,
<<<<<<< HEAD
    val options: List<String>,
    val responses: List<Int>,
=======
    val options: Array<String>,
    val responses: Array<Int>,
>>>>>>> ce323f5 ([Sprint 1] add question API)
    val answer: Int,
)
