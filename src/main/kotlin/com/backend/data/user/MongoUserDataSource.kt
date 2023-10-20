package com.backend.data.user

import User
import com.backend.data.Constants
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.util.KMongoUtil

class MongoUserDataSource(
    db: CoroutineDatabase
) : UserDataSource {
    private val users = db.getCollection<User>()

    override suspend fun getUserByUsername(username: String): User? {
        return users.findOne(User::username eq username)
    }

    override suspend fun insertUser(user: User): Boolean {
        val existingUser = users.findOne(User::username eq user.username);

        if (existingUser != null) { return false; }

        return users.insertOne(user).wasAcknowledged()
    }

    override suspend fun getUsers(): List<User> {
        return  users.find().toList();
    }

    override suspend fun getTeachers(): List<User> {
        return users.find(User::role eq Constants.TEACHER_ROLE).toList();
    }

    override suspend fun getStudents(): List<User> {
        return users.find(User::role eq Constants.STUDENT_ROLE).toList();
    }


}