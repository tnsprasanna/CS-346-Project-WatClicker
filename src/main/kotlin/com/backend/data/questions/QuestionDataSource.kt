package com.backend.data.questions

interface QuestionDataSource {
    suspend fun addQuestion(question: Question): Boolean
}