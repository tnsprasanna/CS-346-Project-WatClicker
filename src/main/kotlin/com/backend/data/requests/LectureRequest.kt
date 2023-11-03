package com.backend.data.requests

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class LectureRequest (
    val name: String,
    val teacherId: String,
    )