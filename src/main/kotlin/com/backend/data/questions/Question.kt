package com.backend.data.questions

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Question (
    @BsonId val id: ObjectId = ObjectId(),
    var question: String,
    var options: MutableList<String>,
    var responses: MutableList<Int>,
    var answer: Int,
    var selections: MutableList<ObjectId>
)
