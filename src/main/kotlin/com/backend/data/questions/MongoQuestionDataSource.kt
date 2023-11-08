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

    private fun getQuestionObjectId(questionId: String): ObjectId? {
        return try { ObjectId(questionId) } catch (e: Exception) { null }
    }

    override suspend fun getQuestionById(questionId: String): Question? {
        val questionObjectId = getQuestionObjectId(questionId)?: return null
        return questions.findOneById(questionObjectId)
    }

    override suspend fun insertQuestion(question: Question): Boolean {
        return questions.insertOne(question).wasAcknowledged()
    }

    override suspend fun deleteQuestion(questionId: String): Boolean {
        val questionObjectId = getQuestionObjectId(questionId)?: return false
        return questions.deleteOneById(questionObjectId).wasAcknowledged()
    }


//    override suspend fun addSelectionToQuestion(questionId: String, selectionId: String): Boolean {
//        val filter = Filters.eq("questionId", questionId)
//        val update = Updates.addToSet(Question::selections.name, selectionId)
//        return questions.updateOne(filter, update).wasAcknowledged();
//    }

}