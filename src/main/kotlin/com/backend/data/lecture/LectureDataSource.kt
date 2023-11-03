package com.backend.data.lecture

import Lecture
interface LectureDataSource {

    suspend fun getLectureByName(lectureId: String): Lecture?

    suspend fun getLectureByID(lectureId: String): Lecture?
    suspend fun createLecture(lecture: Lecture): Boolean
    suspend fun deleteLecture(lectureId: String): String

}
