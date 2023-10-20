package com.backend.data.questions

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Question (
    @BsonId val id: ObjectId = ObjectId(),
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
