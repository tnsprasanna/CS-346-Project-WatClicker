package com.backend.data.questions

import Question
import com.backend.data.selection.Selection
import com.backend.data.user.UserDataSource
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoQuestionDataSource(
    db: CoroutineDatabase
) : QuestionDataSource {
    private val questions = db.getCollection<Question>()
    private val selections = db.getCollection<Selection>()

    private fun getQuestionObjectId(questionId: String): ObjectId? {
        return try { ObjectId(questionId) } catch (e: Exception) { null }
    }

    override suspend fun getQuestionById(questionId: String): Question? {
        println("1")
        val questionObjectId = getQuestionObjectId(questionId)?: return null
        println("2")
        return questions.findOneById(questionObjectId)
    }

    override suspend fun insertQuestion(question: Question): Boolean {
        return questions.insertOne(question).wasAcknowledged()
    }

    override suspend fun editQuestion(questionId: String, question: String, options: MutableList<String>, answer: Int): Boolean? {
        val questionObjectId = getQuestionObjectId(questionId)?: return null
        val oldQuestion = questions.findOneById(questionObjectId)?: return null

        oldQuestion.question = question;
        oldQuestion.options = options;
        oldQuestion.answer = answer;

        // Update all Selection Models
        val selectionsList = selections.find(Selection::questionId eq questionObjectId).toList()

        for (selection in selectionsList) {
            selection.isCorrect = (selection.selectedOption == answer)
            selections.updateOneById(selection.id, selection)
        }

        return questions.updateOneById(questionObjectId, oldQuestion).wasAcknowledged();
    }

    override suspend fun deleteQuestion(questionId: String): Boolean {
        val questionObjectId = getQuestionObjectId(questionId)?: return false
        return questions.deleteOneById(questionObjectId).wasAcknowledged()
    }

    override suspend fun getResponsesFromQuestion(questionId: String): MutableList<Int>? {
        val questionObjectId = getQuestionObjectId(questionId)?: return null
        return questions.findOneById(questionObjectId)?.responses
    }

    override suspend fun addStat(questionId: String, selectedOption: Int): Boolean? {
        val questionObjectId = getQuestionObjectId(questionId)?: return null
        val question = questions.findOneById(questionObjectId)?: return null

        if (selectedOption >= question.options.size || selectedOption < 0) {
            return null
        }

        question.responses[selectedOption] += 1

        return questions.updateOneById(questionObjectId, question).wasAcknowledged()
    }

    override suspend fun removeStat(questionId: String, selectedOption: Int): Boolean? {
        val questionObjectId = getQuestionObjectId(questionId)?: return null
        val question = questions.findOneById(questionObjectId)?: return null

        if (selectedOption >= question.responses.size || 0 > selectedOption) {
            return null
        }

        question.responses[selectedOption] -= 1

        return questions.updateOneById(questionObjectId, question).wasAcknowledged()
    }

    override suspend fun changeStat(questionId: String, oldOption: Int, newOption: Int): Boolean? {
        val questionObjectId = getQuestionObjectId(questionId)?: return null
        val question = questions.findOneById(questionObjectId)?: return null

        if (oldOption >= question.responses.size || 0 > oldOption) {
            return null
        }

        if (newOption >= question.responses.size || 0 > newOption) {
            return null
        }

        question.responses[oldOption] -= 1
        question.responses[newOption] += 1

        return questions.updateOneById(questionObjectId, question).wasAcknowledged()
    }

}