package com.backend.data.responses

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class GetQuizByIdResponse(
    val quizId: String = String.toString(),
    val name: String,
    val state: String,
    val questionIds: Array<String> = emptyArray(),
    val lectureId: String
)
