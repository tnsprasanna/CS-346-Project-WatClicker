package com.backend.data.lecture
import Lecture
import com.backend.data.questions.Question
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import User

class MongoLectureDataSource(
    db: CoroutineDatabase
) : LectureDataSource {
    private val lectures = db.getCollection<Lecture>()

    override suspend fun getLectureByName(name: String): Lecture? {
        return lectures.findOne(Lecture::name eq name)
    }

    override suspend fun getLectureByID(lectureId: String): Lecture? {
        return lectures.findOneById(ObjectId(lectureId))
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

    override suspend fun getLectures(lectureIds: List<ObjectId>): List<Lecture> {
        var result = mutableListOf<Lecture>()

        for (lectureId in lectureIds) {
            val lecture = lectures.findOneById(lectureId)?:continue;
            result.add(lecture)
        }

        return result
    }


    override suspend fun addQuizToClassSection(lectureId: String, quizId: String): Boolean {
        val lecture = lectures.findOneById(ObjectId(lectureId))?: return false
        println("TESTSINEHA")
        return try {
            println("TESTSINEHA5")
            lecture.quizIds.add(quizId)
            println("TESTSINEHA2")
            lectures.updateOneById(ObjectId(lectureId), lecture).wasAcknowledged()
        } catch (ex: Exception) {
            println("TESTSINEHA3")
            false
        }
    }

    override suspend fun getClassSectionJoinCode(classSectionId: String): String {
        val lecture = lectures.findOneById(ObjectId(classSectionId))?: return "err"
        return lecture.joinCode
    }

    override suspend fun isClassSectionJoinable(classSectionId: String): Boolean {
        val lecture = lectures.findOneById(ObjectId(classSectionId))?: return false
        return lecture.isJoinable
    }

    override suspend fun makeClassSectionJoinable(classSectionId: String): Boolean {
        val lecture = lectures.findOneById(ObjectId(classSectionId))?: return false
        lecture.isJoinable = true
        return lectures.updateOneById(ObjectId(classSectionId), lecture).wasAcknowledged()
    }

    override suspend fun makeClassSectionUnjoinable(classSectionId: String): Boolean {
        val lecture = lectures.findOneById(ObjectId(classSectionId))?: return false
        lecture.isJoinable = false
        return lectures.updateOneById(ObjectId(classSectionId), lecture).wasAcknowledged()
    }

    override suspend fun addStudentToClassSection(classSectionId: String, studentId: String): Boolean {
        val lecture = lectures.findOneById(ObjectId(classSectionId))?: return false
        return try {
            lecture.studentIds.add(ObjectId(studentId))
            lectures.updateOneById(ObjectId(classSectionId), lecture).wasAcknowledged()
        } catch (ex: Exception) {
            false
        }
    }
}