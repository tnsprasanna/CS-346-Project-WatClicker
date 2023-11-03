package com.backend.data.requests

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class CreateQuizRequest (
    val name: String,
    // must be one of HIDDEN | OPEN | CLOSED | FINISHED
    val state: String,
    // must be a list of IDs of existing questions
    val questionIds: Array<String>,
    val lectureId: String
)