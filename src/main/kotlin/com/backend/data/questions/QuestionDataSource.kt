package com.backend.data.questions

import Selection

interface QuestionDataSource {
    suspend fun addQuestion(question: Question): Boolean
     suspend fun getQuestion(questionId: String): Question?
     suspend fun addSelectionToQuestion(questionId: String, selectionId: String): Boolean
     suspend fun deleteQuestion(questionId: String): Boolean
}