package com.backend.data.classSection

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class ClassSection(
    @BsonId val id: ObjectId = ObjectId(),

    var name: String,
    var isActive: Boolean,
    var studentIds: MutableList<ObjectId>,
    var teacherId: ObjectId,
    var quizIds: MutableList<ObjectId>,
    var joinCode: String,
    var isJoinable: Boolean
)