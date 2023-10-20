package com.backend.data.questions

import com.backend.data.user.UserDataSource
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoQuestionDataSource(
    db: CoroutineDatabase
) : QuestionDataSource {
    private val questions = db.getCollection<Question>()
    override suspend fun addQuestion(question: Question): Boolean {
        return questions.insertOne(question).wasAcknowledged()
    }
}