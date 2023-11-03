package com.backend.data.lecture
import Lecture
import com.backend.data.questions.Question
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

    override suspend fun getLectureByID(lectureId: String): Lecture? {
        return lectures.findOneById(ObjectId(lectureId));
    }

    override suspend fun createLecture(lecture: Lecture): Boolean {
        println("HI!!!!!!!!!!!!!")
        val result = lectures.insertOne(lecture).wasAcknowledged()
        println(result)
        println("HI2!!!!!!!!!!!!!")
        return result

    }

    override suspend fun deleteLecture(lectureId: String): String {
        val filter = Filters.eq("_id", ObjectId(lectureId))
        return lectures.findOneAndDelete(filter)?.id.toString();
    }
}