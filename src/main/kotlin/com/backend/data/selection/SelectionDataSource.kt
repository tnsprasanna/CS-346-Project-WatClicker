package com.backend.data.selection

import Selection

interface SelectionDataSource {

    suspend fun createSelection(selection: Selection): Boolean
    suspend fun getSelectionById(selectionId: String): Selection?
    suspend fun deleteSelection(selectionId: String): Boolean
    suspend fun editSelection(selectionId: String, newOption: Int, isCorrect: Boolean): Boolean

}
