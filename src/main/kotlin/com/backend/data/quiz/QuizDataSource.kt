package com.backend.data.quiz

import Quiz
import Question
import io.ktor.http.cio.*

interface QuizDataSource {

        suspend fun getQuizById(quizId: String): Quiz? // DONE

        suspend fun getQuizzes(): List<Quiz>

        suspend fun getQuizQuestions(quizId: String): List<Question?>

        suspend fun changeQuizState(quizId: String, newState: String): Boolean

        suspend fun insertQuiz(quiz: Quiz): Boolean // DONE

        suspend fun deleteQuiz(quizId: String): Boolean

        suspend fun addQuestionToQuiz(quizId: String, questionId: String): Boolean

        suspend fun removeQuestionFromQuiz(quizId: String, questionId: String): Boolean

        suspend fun changeQuizName(quizId: String, newName: String): Boolean

        suspend fun gradesForAllStudents(quizId: String): String?

        suspend fun gradesForStudent(quizId: String, studentId: String): String?

}