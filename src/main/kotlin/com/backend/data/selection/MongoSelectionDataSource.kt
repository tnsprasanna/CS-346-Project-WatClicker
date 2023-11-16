package com.backend.data.selection

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.eq

class MongoSelectionDataSource(
    db: CoroutineDatabase
) : SelectionDataSource {
    private val selections = db.getCollection<Selection>()

    private fun getSelectionObjectId(selectionId: String): ObjectId? {
        return try { ObjectId(selectionId) } catch (e: Exception) { null }
    }

    override suspend fun getSelectionById(selectionId: String): Selection? {
        val selectionObjectId = getSelectionObjectId(selectionId)?: return null
        return selections.findOneById(selectionObjectId)
    }

    override suspend fun getSelectionByUserAndQuestionId(userId: String, questionId: String): Selection? {
        return selections.findOne(and(Selection::questionId eq ObjectId(questionId), Selection::studentId eq ObjectId(userId)) )
    }

    override suspend fun createSelection(selection: Selection): Boolean {
        return selections.insertOne(selection).wasAcknowledged()
    }

    override suspend fun deleteSelection(selectionId: String): Boolean? {
        val selectionObjectId = getSelectionObjectId(selectionId)?: return null
        return selections.deleteOneById(selectionObjectId).wasAcknowledged()
    }

    override suspend fun editSelection(selectionId: String, newOption: Int, isCorrect: Boolean): Boolean? {
        val selectionObjectId = getSelectionObjectId(selectionId)?: return null
        val selection =  selections.findOneById(selectionObjectId)?: return null

        selection.selectedOption = newOption
        selection.isCorrect = isCorrect

        return selections.updateOneById(selectionObjectId, selection).wasAcknowledged()
    }
}