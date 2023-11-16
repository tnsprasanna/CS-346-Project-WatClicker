package com.backend.data.classSection

import org.litote.kmongo.coroutine.CoroutineDatabase
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import User
import Quiz
import org.litote.kmongo.coroutine.insertOne

class MongoClassSectionDataSource(
    db: CoroutineDatabase
) : ClassSectionDataSource {
    private val classSections = db.getCollection<ClassSection>()
    private val users = db.getCollection<User>()
    private val quizzes = db.getCollection<Quiz>()

    private fun getClassSectionObjectId(classSectionId: String): ObjectId? {
        return try { ObjectId(classSectionId) } catch (e: Exception) { null }
    }

    override suspend fun getClassSectionByJoinCode(classSectionJoinCode: String): ClassSection? {
        return classSections.findOne(ClassSection::joinCode eq classSectionJoinCode)
    }

    override suspend fun getClassSectionById(classSectionId: String): ClassSection? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        return classSections.findOneById(classSectionObjectId)
    }

    override suspend fun createClassSection(classSection: ClassSection): Boolean {
        var existingClass = classSections.findOne(ClassSection::joinCode eq classSection.joinCode)

        while (existingClass != null) {
            classSection.joinCode = (1..8).map { ('a'..'z').random() }.joinToString("")
            existingClass = classSections.findOne(ClassSection::joinCode eq classSection.joinCode)
        }

        return classSections.insertOne(classSection).wasAcknowledged()
    }

    override suspend fun deleteClassSection(classSectionId: String): Boolean {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return false
        return classSections.deleteOneById(classSectionObjectId).wasAcknowledged()
    }

    override suspend fun getClassSectionJoinCode(classSectionId: String): String? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null
        return classSection.joinCode
    }

    override suspend fun isClassSectionJoinable(classSectionId: String): Boolean? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null
        return classSection.isJoinable
    }

    override suspend fun makeClassSectionJoinable(classSectionId: String): Boolean? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null

        classSection.isJoinable = true

        return classSections.updateOneById(classSectionObjectId, classSection).wasAcknowledged()
    }

    override suspend fun makeClassSectionUnjoinable(classSectionId: String): Boolean? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null

        classSection.isJoinable = false

        return classSections.updateOneById(classSectionObjectId, classSection).wasAcknowledged()
    }

    override suspend fun getClassSectionTeacher(classSectionId: String): User? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null

        return users.findOneById(classSection.teacherId)
    }

    override suspend fun getStudentsInClassSection(classSectionId: String): List<User?>? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null

        return classSection.studentIds.map { users.findOneById(it) }
    }

    override suspend fun addStudentToClassSection(classSectionId: String, studentId: String): Boolean? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null
        val studentObjectId = try { ObjectId(classSectionId) } catch (e: Exception) { null }?: return null

        return try {
            classSection.studentIds.add(studentObjectId)
            classSections.updateOneById(classSectionObjectId, classSection).wasAcknowledged()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeStudentFromClassSection(classSectionId: String, studentId: String): Boolean? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null
        val studentObjectId = try { ObjectId(classSectionId) } catch (e: Exception) { null }?: return null

        return try {
            classSection.studentIds.remove(studentObjectId)
            classSections.updateOneById(classSectionObjectId, classSection).wasAcknowledged()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addQuizToClassSection(classSectionId: String, quizId: String): Boolean? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null
        val quizObjectId = try { ObjectId(classSectionId) } catch (e: Exception) { null }?: return null

        return try {
            classSection.quizIds.add(quizObjectId)
            classSections.updateOneById(classSectionObjectId, classSection).wasAcknowledged()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeQuizFromClassSection(classSectionId: String, quizId: String): Boolean? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null
        val quizObjectId = try { ObjectId(classSectionId) } catch (e: Exception) { null }?: return null

        return try {
            classSection.quizIds.remove(quizObjectId)
            classSections.updateOneById(classSectionObjectId, classSection).wasAcknowledged()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getQuizzes(classSectionId: String): List<Quiz?>? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null

        return classSection.quizIds.map { quizzes.findOneById(it) }
    }

    override suspend fun getClassSectionActiveStatus(classSectionId: String): Boolean? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null

        return classSection.isActive
    }

    override suspend fun makeClassSectionActive(classSectionId: String): Boolean? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null

        classSection.isActive = true

        return classSections.updateOneById(classSectionObjectId, classSection).wasAcknowledged()
    }

    override suspend fun makeClassSectionInactive(classSectionId: String): Boolean? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null

        classSection.isJoinable = false

        return classSections.updateOneById(classSectionObjectId, classSection).wasAcknowledged()
    }

    override suspend fun changeClassSectionName(classSectionId: String, newName: String): Boolean? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSection = classSections.findOneById(classSectionObjectId)?: return null

        classSection.name = newName

        return classSections.updateOneById(classSectionObjectId, classSection).wasAcknowledged()
    }

}
