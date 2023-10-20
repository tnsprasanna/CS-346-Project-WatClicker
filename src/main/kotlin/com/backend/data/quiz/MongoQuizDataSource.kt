package com.backend.data.quiz

import Quiz
import User
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoQuizDataSource(
    db: CoroutineDatabase
) : QuizDataSource {
    private val quizzes = db.getCollection<Quiz>()

    override suspend fun getQuiz(quizId: String): Quiz? {
        TODO("Not yet implemented")
    }

    override suspend fun createQuiz(quiz: Quiz): Boolean {
       return quizzes.insertOne(quiz).wasAcknowledged();
    }
}