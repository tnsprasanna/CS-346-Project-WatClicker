package com.backend.data.responses

import User
import Lecture

import org.bson.codecs.pojo.annotations.BsonId
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class ClassSectionResponse(
    val id: String,
    val name: String,
    val teacherId: String,
    val studentIds: List<String>,
    val isActive: Boolean,
    val quizIds: List<String>,
    val joinCode: String,
    val isJoinable: Boolean
)

