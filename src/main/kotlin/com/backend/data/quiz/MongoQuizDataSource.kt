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

    // works
    override suspend fun getQuizQuestions(quizId: String): Quiz? {
        val filter = Filters.eq("quizId", quizId)
        return quizzes.find(filter).first();
    }

    override suspend fun getQuizzes(): List<Quiz> {
        return quizzes.find().toList();
    }

    // works
    override suspend fun changeState(quizId: String, newState: String): String {
        val filter = Filters.eq("quizId", quizId)
        //val filter = Filters.eq("_id", ObjectId(quizId))
        val update = Updates.set(Quiz::state.name, newState)
        return quizzes.updateOne(filter, update).toString();
    }

    //works
    override suspend fun deleteQuiz(quizId: String): String {
        val filter = Filters.eq("quizId", quizId)
        return quizzes.findOneAndDelete(filter)?.quizId.toString();
    }

    //works
    override suspend fun createQuiz(quiz: Quiz): Boolean {
       return quizzes.insertOne(quiz).wasAcknowledged();
    }
}