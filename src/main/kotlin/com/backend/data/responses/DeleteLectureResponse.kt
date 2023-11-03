package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
class DeleteLectureResponse (
    val deletedLecture: String
)
