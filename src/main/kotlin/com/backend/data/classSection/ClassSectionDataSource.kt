package com.backend.data.classSection

import Quiz
import User
import org.bson.types.ObjectId

interface ClassSectionDataSource {
    suspend fun getClassSectionById(classSectionId: String): ClassSection?

    suspend fun getClassSectionByJoinCode(classSectionJoinCode: String): ClassSection?

    suspend fun createClassSection(classSection: ClassSection): Boolean

    suspend fun deleteClassSection(classSectionId: String): Boolean

    suspend fun getClassSectionJoinCode(classSectionId: String): String?

    suspend fun isClassSectionJoinable(classSectionId: String): Boolean?

    suspend fun makeClassSectionJoinable(classSectionId: String): Boolean?

    suspend fun makeClassSectionUnjoinable(classSectionId: String): Boolean?

    suspend fun getClassSectionTeacher(classSectionId: String): User?

    suspend fun getStudentsInClassSection(classSectionId: String): List<User?>?

    suspend fun addStudentToClassSection(classSectionId: String, studentId: String): Boolean?

    suspend fun removeStudentFromClassSection(classSectionId: String, studentId: String): Boolean?

    suspend fun addQuizToClassSection(classSectionId: String, quizId: String): Boolean?

    suspend fun removeQuizFromClassSection(classSectionId: String, quizId: String): Boolean?

    suspend fun getQuizzes(classSectionId: String): List<Quiz?>?

    suspend fun getClassSectionActiveStatus(classSectionId: String): Boolean?

    suspend fun makeClassSectionActive(classSectionId: String): Boolean?

    suspend fun makeClassSectionInactive(classSectionId: String): Boolean?

    suspend fun changeClassSectionName(classSectionId: String, newName: String): Boolean?

    suspend fun gradesForAllStudents(classSectionId: String): String?

    suspend fun gradesForStudent(classSectionId: String, studentId: String): String?

}


/*


interface LectureDataSource {
    suspend fun getLectureByName(lectureId: String): Lecture?
    suspend fun getLectures(lectureIds: List<ObjectId>): List<Lecture>
    suspend fun getLectureQuizzes(lectureId: ObjectId): Boolean
}

 */