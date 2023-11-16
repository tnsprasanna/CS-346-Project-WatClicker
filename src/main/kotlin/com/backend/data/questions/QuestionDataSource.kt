package com.backend.data.questions

import Selection

interface QuestionDataSource {

     suspend fun getQuestionById(questionId: String): Question?

     suspend fun insertQuestion(question: Question): Boolean

     suspend fun deleteQuestion(questionId: String): Boolean

     suspend fun getResponsesFromQuestion(questionId: String): Question?

     // methods to edit every editable quiz field

}