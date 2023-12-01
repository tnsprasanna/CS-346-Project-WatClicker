package com.backend.data.quiz

import Quiz
import Question
import com.backend.data.Constants
import org.litote.kmongo.coroutine.CoroutineDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.insertOne

class MongoQuizDataSource(
    db: CoroutineDatabase
) : QuizDataSource {
    private val quizzes = db.getCollection<Quiz>()
    private val questions = db.getCollection<Question>()
    private fun getQuizObjectId(quizId: String): ObjectId? {
        return try { ObjectId(quizId) } catch (e: Exception) { null }
    }

    override suspend fun getQuizById(quizId: String): Quiz? {
        val quizObjectId = getQuizObjectId(quizId)?: return null
        return quizzes.findOneById(quizObjectId)
    }

    override suspend fun getQuizzes(): List<Quiz> {
        return quizzes.find().toList();
    }

    override suspend fun getQuizQuestions(quizId: String): List<Question?> {
        val quizObjectId = getQuizObjectId(quizId)?: return emptyList()
        val quiz = quizzes.findOneById(quizObjectId)?: return emptyList()

        return quiz.questionIds.map { questions.findOneById(it) }
    }

    override suspend fun changeQuizState(quizId: String, newState: String): Boolean {
        val quizObjectId = getQuizObjectId(quizId)?: return false
        val quiz = quizzes.findOneById(quizObjectId)?: return false

        if (!(Constants.QUIZ_STATES.contains(newState))) { return false }

        quiz.state = newState

        return quizzes.updateOneById(quizObjectId, quiz).wasAcknowledged()
    }

    override suspend fun insertQuiz(quiz: Quiz): Boolean {
        return quizzes.insertOne(quiz).wasAcknowledged()
    }

    override suspend fun deleteQuiz(quizId: String): Boolean {
        val quizObjectId = getQuizObjectId(quizId)?: return false
        return quizzes.deleteOneById(quizObjectId).wasAcknowledged()
    }

    override suspend fun addQuestionToQuiz(quizId: String, questionId: String): Boolean {
        val quizObjectId = getQuizObjectId(quizId)?: return false
        val quiz = quizzes.findOneById(quizObjectId)?: return false

        var questionInQuiz = false;
        for (qId in quiz.questionIds) {
            if (qId.toString() == questionId) {
                questionInQuiz = true
                break
            }
        }

        if (questionInQuiz) { return false }

        return try {
            quiz.questionIds.add(ObjectId(questionId))
            quizzes.updateOneById(quizObjectId, quiz).wasAcknowledged()
        } catch (e: Exception) { false }
    }

    override suspend fun removeQuestionFromQuiz(quizId: String, questionId: String): Boolean {
        val quizObjectId = getQuizObjectId(quizId)?: return false
        val quiz = quizzes.findOneById(quizObjectId)?: return false

        var questionInQuiz = false;
        for (qId in quiz.questionIds) {
            if (qId.toString() == questionId) {
                questionInQuiz = true
                break
            }
        }

        if (!questionInQuiz) { return false }

        return try {
            quiz.questionIds.remove(ObjectId(questionId))
            quizzes.updateOneById(quizObjectId, quiz).wasAcknowledged()
        } catch (e: Exception) { false }
    }

    override suspend fun changeQuizName(quizId: String, newName: String): Boolean {
        val quizObjectId = getQuizObjectId(quizId)?: return false
        val quiz = quizzes.findOneById(quizObjectId)?: return false

        quiz.name = newName

        return quizzes.updateOneById(quizObjectId, quiz).wasAcknowledged()
    }

    override suspend fun gradesForAllStudents(quizId: String): String? {
        return "NEED_TO_IMPLEMENT"
    }

    override suspend fun gradesForStudent(quizId: String, studentId: String): String? {
        return "NEED_TO_IMPLEMENT"
    }
}