package com.backend.data.selection

import Selection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.litote.kmongo.coroutine.CoroutineDatabase

class MongoSelectionDataSource(
    db: CoroutineDatabase
) : SelectionDataSource {
    private val selections = db.getCollection<Selection>()

    override suspend fun createSelection(selection: Selection): Boolean {
       return selections.insertOne(selection).wasAcknowledged();
    }

    override suspend fun getSelectionById(selectionId: String): Selection? {
        val filter = Filters.eq("selectionId", selectionId)
        return selections.findOne(filter);
    }
    override suspend fun deleteSelection(selectionId: String): Boolean {
        val filter = Filters.eq("selectionId", selectionId)
        return selections.deleteOne(filter).wasAcknowledged();
    }
   override suspend fun editSelection(selectionId: String, newOption: Int, isCorrect: Boolean): Boolean {
       val filter = Filters.eq("selectionId", selectionId)
       val update = Updates.set(Selection::selectedOption.name, newOption)
       val update2 = Updates.set(Selection::isCorrect.name, newOption)
       return selections.updateOne(filter, update).wasAcknowledged() &&
               selections.updateOne(filter, update2).wasAcknowledged();
   }
}