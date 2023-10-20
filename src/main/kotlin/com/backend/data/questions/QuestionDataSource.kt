package com.backend.data.questions

interface QuestionDataSource {
    suspend fun addQuestion(question: Question): Boolean
     suspend fun getQuestion(questionId: String): Question?
     suspend fun deleteQuestion(questionId: String): Boolean
}