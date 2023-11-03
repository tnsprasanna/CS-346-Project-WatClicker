package com.backend.data.lecture

import Lecture
import org.bson.types.ObjectId

interface LectureDataSource {

    suspend fun getLectureByName(lectureId: String): Lecture?

    suspend fun getLectureByID(lectureId: String): Lecture?
    suspend fun createLecture(lecture: Lecture): Boolean
    suspend fun deleteLecture(lectureId: String): String
    suspend fun getLectures(lectureIds: List<ObjectId>): List<Lecture>

    suspend fun getClassSectionJoinCode(classSectionId: String): String

    suspend fun isClassSectionJoinable(classSectionId: String): Boolean

    suspend fun makeClassSectionJoinable(classSectionId: String): Boolean

    suspend fun makeClassSectionUnjoinable(classSectionId: String): Boolean

    suspend fun addStudentToClassSection(classSectionId: String, studentId: String): Boolean

    suspend fun addQuizToClassSection(lectureId: String, quizId: String): Boolean

}
