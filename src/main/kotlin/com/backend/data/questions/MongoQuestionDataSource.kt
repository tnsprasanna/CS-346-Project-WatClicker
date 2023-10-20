package com.backend.data.questions

import com.backend.data.user.UserDataSource
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoQuestionDataSource(
    db: CoroutineDatabase
) : QuestionDataSource {
    private val questions = db.getCollection<Question>()
    override suspend fun addQuestion(question: Question): Boolean {
        return questions.insertOne(question).wasAcknowledged()
    }

    override suspend fun getQuestion(questionId: String): Question? {
        return questions.findOneById(ObjectId(questionId))
    }

    override suspend fun deleteQuestion(questionId: String): Boolean {
        val result = questions.deleteOneById(ObjectId(questionId))
        return result.wasAcknowledged()
    }
}