package com.backend.data.lecture
import Lecture
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoLectureDataSource(
    db: CoroutineDatabase
) : LectureDataSource {
    private val lectures = db.getCollection<Lecture>()

    override suspend fun getLectureByName(name: String): Lecture? {
        return lectures.findOne(Lecture::name eq name)
    }

    override suspend fun addLecture(lecture: Lecture): Boolean {
        return lectures.insertOne(lecture).wasAcknowledged()
    }
}