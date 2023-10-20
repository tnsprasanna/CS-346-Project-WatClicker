package com.backend.data.lecture

import Lecture
interface LectureDataSource {
    suspend fun getLectureByName(name: String): Lecture?
    suspend fun addLecture(lecture: Lecture): Boolean
}
