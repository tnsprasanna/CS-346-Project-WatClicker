package com.backend.data.quiz

import Quiz

interface QuizDataSource {
        suspend fun getQuizQuestions(quizId: String): Quiz?

        suspend fun getQuizzes(): List<Quiz>

        suspend fun changeState(quizId: String, newState: String): String

        suspend fun createQuiz(quiz: Quiz): Boolean

        suspend fun deleteQuiz(quizId: String): String
    }