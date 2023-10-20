package com.backend.data.quiz

import Quiz

interface QuizDataSource {
        suspend fun getQuiz(quizId: String): Quiz?

        suspend fun changeState(quizId: String, newState: String): String

        suspend fun createQuiz(quiz: Quiz): Boolean

        suspend fun deleteQuiz(quizId: String): String
    }