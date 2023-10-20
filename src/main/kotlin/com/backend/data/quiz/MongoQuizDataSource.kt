package com.backend.data.quiz

import Quiz
import org.litote.kmongo.coroutine.CoroutineDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.bson.types.ObjectId

class MongoQuizDataSource(
    db: CoroutineDatabase
) : QuizDataSource {
    private val quizzes = db.getCollection<Quiz>()

    override suspend fun getQuiz(quizId: String): Quiz? {
        return quizzes.findOneById(ObjectId(quizId));
    }

    override suspend fun changeState(quizId: String, newState: String): String {
        val filter = Filters.eq("_id", ObjectId(quizId))
        val update = Updates.set(Quiz::state.name, newState)
        return quizzes.updateOne(filter, update).toString();
    }

    override suspend fun deleteQuiz(quizId: String): String {
        val filter = Filters.eq("_id", ObjectId(quizId))
        return quizzes.findOneAndDelete(filter)?.id.toString();
    }

    override suspend fun createQuiz(quiz: Quiz): Boolean {
       return quizzes.insertOne(quiz).wasAcknowledged();
    }
}