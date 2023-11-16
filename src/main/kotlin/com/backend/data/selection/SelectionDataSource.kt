package com.backend.data.selection

interface SelectionDataSource {

    suspend fun getSelectionById(selectionId: String): Selection?

    suspend fun getSelectionByUserAndQuestionId(userId: String, questionId: String): Selection?

    suspend fun createSelection(selection: Selection): Boolean

    suspend fun deleteSelection(selectionId: String): Boolean?

    suspend fun editSelection(selectionId: String, newOption: Int, isCorrect: Boolean): Boolean?

}
