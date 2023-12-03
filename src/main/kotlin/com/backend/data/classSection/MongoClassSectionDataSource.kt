package com.backend.data.classSection

import org.litote.kmongo.coroutine.CoroutineDatabase
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import User
import Quiz
import com.backend.data.Constants
import com.backend.data.selection.Selection
import org.litote.kmongo.coroutine.insertOne

class MongoClassSectionDataSource(
    db: CoroutineDatabase
) : ClassSectionDataSource {
    private val classSections = db.getCollection<ClassSection>()
    private val users = db.getCollection<User>()
    private val quizzes = db.getCollection<Quiz>()
    private val selections = db.getCollection<Selection>()

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
        val studentObjectId = try { ObjectId(studentId) } catch (e: Exception) { null }?: return null

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
        val quizObjectId = try { ObjectId(quizId) } catch (e: Exception) { null }?: return null

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

    override suspend fun gradesForAllStudents(classSectionId: String): String? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSectionObject =  classSections.findOneById(classSectionObjectId)?: return null

        val quizNames: MutableList<String> = mutableListOf("")
        val grades: HashMap<String, MutableList<String>> = HashMap()

        for (studentId in classSectionObject.studentIds) {
            val studentObject = users.findOneById(studentId)?: continue
            grades[studentObject.username] = mutableListOf()

            for (quizId in classSectionObject.quizIds) {
                val quizObject = quizzes.findOneById(quizId)?: continue

                if (!quizNames.contains(quizObject.name)) {
                    quizNames.add(quizObject.name)
                }

                var numCorrect = 0.0
                var numQuestions = 0.0

                for (questionIds in quizObject.questionIds) {
                    numQuestions += 1.0;

                    val selectionObject = selections.findOne(
                        Selection::questionId eq questionIds, Selection::studentId eq studentId)?: continue

                    if (selectionObject.isCorrect) {
                        numCorrect += 1
                    }
                }

                grades[studentObject.username]?.add(Constants.getPercentage(numCorrect, numQuestions))
            }
        }


        return Constants.generateCSVForList(quizNames, grades)
    }

    override suspend fun gradesForStudent(classSectionId: String, studentId: String): String? {
        val classSectionObjectId = getClassSectionObjectId(classSectionId)?: return null
        val classSectionObject =  classSections.findOneById(classSectionObjectId)?: return null

        val quizNames: MutableList<String> = mutableListOf("")
        val grades: HashMap<String, MutableList<String>> = HashMap()

        val studentObject = users.findOneById(ObjectId(studentId))?: return "ERROR"
        grades[studentObject.username] = mutableListOf()

        for (quizId in classSectionObject.quizIds) {
            val quizObject = quizzes.findOneById(quizId)?: continue

            if (!quizNames.contains(quizObject.name)) {
                quizNames.add(quizObject.name)
            }

            var numCorrect = 0.0
            var numQuestions = 0.0

            for (questionIds in quizObject.questionIds) {
                numQuestions += 1.0;

                val selectionObject = selections.findOne(
                    Selection::questionId eq questionIds, Selection::studentId eq studentObject.id)?: continue

                if (selectionObject.isCorrect) {
                    numCorrect += 1
                }
            }

            grades[studentObject.username]?.add(Constants.getPercentage(numCorrect, numQuestions))
        }


        return Constants.generateCSVForList(quizNames, grades)
    }
}
