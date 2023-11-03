package com.backend.data.lecture
import Lecture
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoLectureDataSource(
    db: CoroutineDatabase
) : LectureDataSource {
    private val lectures = db.getCollection<Lecture>()

    override suspend fun getLectureByName(name: String): Lecture? {
        return lectures.findOne(Lecture::name eq name)
    }

    override suspend fun createLecture(lecture: Lecture): Boolean {
        return lectures.insertOne(lecture).wasAcknowledged()
    }

    override suspend fun deleteLecture(lectureId: String): String {
        val filter = Filters.eq("_id", ObjectId(lectureId))
        return lectures.findOneAndDelete(filter)?.id.toString();
    }
}