package com.backend.data.user

import User
import com.backend.data.Constants
import com.typesafe.config.ConfigException.Null
import kotlinx.coroutines.runBlocking
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.util.KMongoUtil
import org.bson.types.ObjectId

class MongoUserDataSource(
    db: CoroutineDatabase
) : UserDataSource {
    private val users = db.getCollection<User>()

    override suspend fun getUserById(userId: String): User? {
        if (userId.isEmpty()) return null
       return users.findOneById(ObjectId(userId))
    }

    override suspend fun getUserByUsername(username: String): User? {
        return users.findOne(User::username eq username)
    }

    override suspend fun insertUser(user: User): Boolean {
        val existingUser = users.findOne(User::username eq user.username);

        if (existingUser != null) { return false; }

        return users.insertOne(user).wasAcknowledged()
    }

    override suspend fun getUsers(): List<User> {
        return users.find().toList();
    }

    override suspend fun getTeachers(): List<User> {
        return users.find(User::role eq Constants.TEACHER_ROLE).toList();
    }

    override suspend fun getStudents(): List<User> {
        return users.find(User::role eq Constants.STUDENT_ROLE).toList();
    }

    override suspend fun isStudentFromId(userId: String): Boolean {
        val user = users.findOneById(ObjectId(userId)) ?: return false;
        return user.role == Constants.STUDENT_ROLE
    }

    override suspend fun isStudentFromUsername(username: String): Boolean {
        val user = users.findOne(User::username eq username) ?: return false;
        return user.role == Constants.STUDENT_ROLE
    }

    override suspend fun isTeacherFromId(userId: String): Boolean {
        val user = users.findOneById(ObjectId(userId)) ?: return false;
        return user.role == Constants.TEACHER_ROLE
    }

    override suspend fun isTeacherFromUsername(username: String): Boolean {
        val user = users.findOne(User::username eq username) ?: return false;
        return user.role == Constants.TEACHER_ROLE
    }

    override suspend fun deleteUser(userId: String): Boolean {
        return users.deleteOneById(ObjectId(userId)).wasAcknowledged()
    }

    override suspend fun changeRole(userId: String, newRole: String): Boolean {
        if (newRole != Constants.TEACHER_ROLE && newRole != Constants.STUDENT_ROLE) { return false; }

        val user = users.findOneById(ObjectId(userId))?: return false;
        user.role = newRole;
        return users.updateOneById(ObjectId(userId), user).wasAcknowledged();
    }

    override suspend fun changeFirstName(userId: String, newFirstName: String): Boolean {
        val user = users.findOneById(ObjectId(userId))?: return false;
        user.firstname = newFirstName;
        return users.updateOneById(ObjectId(userId), user).wasAcknowledged();
    }

    override suspend fun changeLastName(userId: String, newLastName: String): Boolean {
        val user = users.findOneById(ObjectId(userId))?: return false;
        user.lastname = newLastName;
        return users.updateOneById(ObjectId(userId), user).wasAcknowledged();
    }

    override suspend fun changeFirstAndLastName(userId: String, newFirstName: String, newLastName: String): Boolean {
        val user = users.findOneById(ObjectId(userId))?: return false;
        user.firstname = newFirstName;
        user.lastname = newLastName;
        return users.updateOneById(ObjectId(userId), user).wasAcknowledged();
    }

    override suspend fun changeUsername(userId: String, newUsername: String): Boolean {
        users.findOne(User::username eq newUsername)?: return false
        val user = users.findOneById(ObjectId(userId))?: return false;
        user.username = newUsername;
        return users.updateOneById(ObjectId(userId), user).wasAcknowledged();
    }

    override suspend fun changePassword(userId: String, newPassword: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun addClassSectionToStudent(studentId: String, classSectionId: String): Boolean {
        val user = users.findOneById(ObjectId(studentId))?: return false
        return try {
            user.classSectionList.add(ObjectId(classSectionId))
            users.updateOneById(ObjectId(studentId), user).wasAcknowledged()
        } catch (ex: Exception) {
            false
        }
    }
}