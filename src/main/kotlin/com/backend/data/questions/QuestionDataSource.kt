package com.backend.data.questions

import Question

interface QuestionDataSource {

     suspend fun getQuestionById(questionId: String): Question?

     suspend fun insertQuestion(question: Question): Boolean

     suspend fun editQuestion(questionId: String, question: String, options: MutableList<String>, answer: Int): Boolean?

     suspend fun deleteQuestion(questionId: String): Boolean

     suspend fun getResponsesFromQuestion(questionId: String): MutableList<Int>?
     suspend fun addStat(questionId: String, selectedOption: Int): Boolean?

     suspend fun removeStat(questionId: String, selectedOption: Int): Boolean?
     suspend fun changeStat(questionId: String, oldOption: Int, newOption: Int): Boolean?

     // methods to edit every editable quiz field


}