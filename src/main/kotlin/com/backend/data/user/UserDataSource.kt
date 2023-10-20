package com.backend.data.user

import User

interface UserDataSource {
    suspend fun getUserByUsername(username: String): User?

    suspend fun insertUser(user: User): Boolean

    suspend fun getUsers(): List<User>

    suspend fun getTeachers(): List<User>

    suspend fun getStudents(): List<User>



    // Get User by ID

    // Update User

    // Delete User
}