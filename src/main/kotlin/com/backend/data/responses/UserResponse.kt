package com.backend.data.responses

import User
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val role: String,
    val firstname: String,
    val lastname: String,
    val classSectionList: List<String>
)
