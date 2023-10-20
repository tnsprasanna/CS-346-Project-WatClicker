package com.backend.data.questions

interface QuestionDataSource {
    suspend fun addQuestion(question: Question): Boolean
<<<<<<< HEAD
    suspend fun deleteQuestion(questionId: String): Boolean
    suspend fun getQuestion(questionId: String): Question?
=======
>>>>>>> ce323f5 ([Sprint 1] add question API)
}