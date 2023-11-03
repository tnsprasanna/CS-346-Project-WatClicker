package com.backend.data.questions

import Selection
import com.backend.data.user.UserDataSource
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
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

    override suspend fun addSelectionToQuestion(questionId: String, selectionId: String): Boolean {
        val filter = Filters.eq("questionId", questionId)
        val update = Updates.addToSet(Question::selections.name, selectionId)
        return questions.updateOne(filter, update).wasAcknowledged();
    }

    override suspend fun deleteQuestion(questionId: String): Boolean {
        val filter = Filters.eq("questionId", questionId)
        val result = questions.deleteOne(filter)
        return result.wasAcknowledged()
    }
}