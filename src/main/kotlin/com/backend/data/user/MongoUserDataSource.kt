package com.backend.data.user

import User
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

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
}