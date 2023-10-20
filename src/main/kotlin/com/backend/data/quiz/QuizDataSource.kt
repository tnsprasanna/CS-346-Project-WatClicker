package com.backend.data.quiz

import Quiz

interface QuizDataSource {
        suspend fun getQuiz(quizId: String): Quiz?

        suspend fun createQuiz(quiz: Quiz): Boolean
    }