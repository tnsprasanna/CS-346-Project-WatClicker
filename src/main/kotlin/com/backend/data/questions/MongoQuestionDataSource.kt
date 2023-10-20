package com.backend.data.questions

import com.backend.data.user.UserDataSource
<<<<<<< HEAD
import org.bson.types.ObjectId
=======
>>>>>>> ce323f5 ([Sprint 1] add question API)
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoQuestionDataSource(
    db: CoroutineDatabase
) : QuestionDataSource {
    private val questions = db.getCollection<Question>()
    override suspend fun addQuestion(question: Question): Boolean {
<<<<<<< HEAD
        val q = questions.insertOne(question)
        return q.wasAcknowledged()
    }
    override suspend fun getQuestion(questionId: String): Question? {
        return questions.findOneById(ObjectId(questionId))
    }
    override suspend fun deleteQuestion(questionId: String): Boolean {
        val result = questions.deleteOneById(ObjectId(questionId))
        return result.wasAcknowledged()
=======
        return questions.insertOne(question).wasAcknowledged()
>>>>>>> ce323f5 ([Sprint 1] add question API)
    }
}