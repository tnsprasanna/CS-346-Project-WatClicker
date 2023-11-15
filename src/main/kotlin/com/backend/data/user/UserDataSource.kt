package com.backend.data.user

import User
import org.bson.codecs.pojo.annotations.BsonId

interface UserDataSource {
    suspend fun getUserById(userId: String): User?

    suspend fun getUserByUsername(username: String): User?

    suspend fun insertUser(user: User): Boolean

    suspend fun getUsers(): List<User>

    suspend fun getTeachers(): List<User>

    suspend fun getStudents(): List<User>

    suspend fun isStudentFromId(userId: String): Boolean

    suspend fun isStudentFromUsername(username: String): Boolean

    suspend fun isTeacherFromId(userId: String): Boolean

    suspend fun isTeacherFromUsername(username: String): Boolean

    suspend fun deleteUser(userId: String): Boolean

    suspend fun changeRole(userId: String, newRole: String): Boolean

    suspend fun changeFirstName(userId: String, newFirstName: String): Boolean

    suspend fun changeLastName(userId: String, newLastName: String): Boolean

    suspend fun changeFirstAndLastName(userId: String, newFirstName: String, newLastName: String): Boolean

    suspend fun changeUsername(userId: String, newUsername: String): Boolean

    suspend fun changePassword(userId: String, newPassword: String, newSalt: String): Boolean

    suspend fun addClassSectionToUser(userId: String, classSectionId: String): Boolean

    suspend fun removeClassSectionFromUser(userId: String, classSectionId: String): Boolean
}