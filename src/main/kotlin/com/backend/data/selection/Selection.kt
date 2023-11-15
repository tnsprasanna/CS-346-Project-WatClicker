package com.backend.data.selection

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Selection(
    @BsonId val id: ObjectId = ObjectId(),
    val questionId: ObjectId,
    var studentId: ObjectId,
    var selectedOption: Int,
    var isCorrect: Boolean,
)
