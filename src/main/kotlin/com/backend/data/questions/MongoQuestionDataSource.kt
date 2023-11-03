package com.backend.data.questions

import com.backend.data.user.UserDataSource
import com.mongodb.client.model.Filters
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
        val filter = Filters.eq("questionId", questionId)
        return questions.findOne(filter);
    }

    override suspend fun deleteQuestion(questionId: String): Boolean {
        val filter = Filters.eq("questionId", questionId)
        val result = questions.deleteOne(filter)
        return result.wasAcknowledged()
    }
}