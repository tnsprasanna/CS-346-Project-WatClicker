package com.backend.data.questions

interface QuestionDataSource {
    suspend fun addQuestion(question: Question): Boolean
    suspend fun deleteQuestion(questionId: String): Boolean
    suspend fun getQuestion(questionId: String): Question?
}