package com.backend.data.questions

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Question (
    @BsonId val id: ObjectId = ObjectId(),
    val questionId: String = String.toString(),
    val question: String,
    val options: List<String> = emptyList(),
    val responses: List<Int> = emptyList(),
    val answer: Int,
    val selections: List<String> = emptyList()
)
