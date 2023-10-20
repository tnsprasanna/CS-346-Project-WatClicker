package com.backend.data.questions

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Question (
    @BsonId val id: ObjectId = ObjectId(),
    val question: String,
    val options: Array<String>,
    val responses: Array<Int>,
    val answer: Int,
)
