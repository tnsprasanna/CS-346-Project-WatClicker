package com.backend.data.requests

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class CreateQuizRequest (
    val name: String,
    val state: String, // must be one of HIDDEN | OPEN | CLOSED | FINISHED
    val questionIds: List<String>, // will be empty
    val classSectionId: String
)