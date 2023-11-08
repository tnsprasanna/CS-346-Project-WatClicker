package com.backend.routes

import User
import com.backend.data.user.UserDataSource
import com.backend.data.Constants
import com.backend.data.lecture.LectureDataSource
import com.backend.data.requests.*
import com.backend.data.responses.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

fun Route.getUsers(
    userDataSource: UserDataSource
) {
    get("getUsers") {
        val userResponses = userDataSource.getUsers().map {
            u -> UserResponse(
                u.id.toString(),
                u.username,
                u.role,
                u.firstname,
                u.lastname,
                u.classSectionList.map { it.toString() }
            )
        };

       call.respond(
            status = HttpStatusCode.OK,
            message = UserListResponse(userResponses)
       )
    }
}

fun Route.getUserById(
    userDataSource: UserDataSource
) {
    get("getUserById") {
        val request = kotlin.runCatching { call.receiveNullable<UserIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
            return@get
        }

        val user = userDataSource.getUserById(request.userId)?: kotlin.run {
            call.respond(HttpStatusCode.Conflict, "User not found!")
            return@get
        }

        call.respond(
            status = HttpStatusCode.OK,
            message = UserResponse(
                user.id.toString(),
                user.username,
                user.role,
                user.firstname,
                user.lastname,
                user.classSectionList.map { it.toString() }
            )
        )
    }
}

fun Route.getTeachers(
    userDataSource: UserDataSource
) {
    get("getTeachers") {
        val teacherResponses = userDataSource.getTeachers().map {
            u -> UserResponse(
                u.id.toString(),
                u.username,
                u.role,
                u.firstname,
                u.lastname,
                u.classSectionList.map { it.toString() }
            )
        };

        call.respond(
            status = HttpStatusCode.OK,
            message = UserListResponse(teacherResponses)
        )
    }
}

fun Route.getStudents(
    userDataSource: UserDataSource
) {
    get("getStudents") {
        val studentResponses = userDataSource.getStudents().map {
            u -> UserResponse(
                u.id.toString(),
                u.username,
                u.role,
                u.firstname,
                u.lastname,
                u.classSectionList.map { it.toString() }
            )
        };

        call.respond(
            status = HttpStatusCode.OK,
            message = UserListResponse(studentResponses)
        )
    }
}

fun Route.isStudentFromId(
    userDataSource: UserDataSource
) {
    get("isStudentFromId") {
        val request = kotlin.runCatching { call.receiveNullable<UserIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
            return@get
        }

        val user = userDataSource.getUserById(request.userId)?: kotlin.run{
            call.respond(HttpStatusCode.Conflict, "User not found!")
            return@get
        }

        if (user.role != Constants.STUDENT_ROLE) {
            call.respond(HttpStatusCode.Conflict, "User is not a student!")
        }

        call.respond(HttpStatusCode.OK, "User is a student")
    }
}

fun Route.isStudentFromUsername(
    userDataSource: UserDataSource
) {
    get("isStudentFromUsername") {
        val request = kotlin.runCatching { call.receiveNullable<UsernameRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
            return@get
        }

        val user = userDataSource.getUserByUsername(request.username)?: kotlin.run {
            call.respond(HttpStatusCode.Conflict, "User not found!")
            return@get
        }

        if (user.role != Constants.STUDENT_ROLE) {
            call.respond(HttpStatusCode.Conflict, "User is not a student!")
        }

        call.respond(HttpStatusCode.OK, "User is a student")
    }
}

fun Route.isTeacherFromId(
    userDataSource: UserDataSource
) {
    get("isTeacherFromId") {
        val request = kotlin.runCatching { call.receiveNullable<UserIdRequest>() }.getOrNull()?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
            return@get
        }

        val user = userDataSource.getUserById(request.userId)?: kotlin.run {
            call.respond(HttpStatusCode.Conflict, "User not found!")
            return@get
        }

        if (user.role != Constants.TEACHER_ROLE) {
            call.respond(HttpStatusCode.Conflict, "User is not a teacher!")
        }

        call.respond(HttpStatusCode.OK, "User is a teacher")
    }
}

fun Route.isTeacherFromUsername(
    userDataSource: UserDataSource
) {
    get("isTeacherFromUsername") {
        val request = kotlin.runCatching { call.receiveNullable<UsernameRequest>() }.getOrNull()?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
            return@get
        }

        val user = userDataSource.getUserByUsername(request.username)?: kotlin.run{
            call.respond(HttpStatusCode.Conflict, "User not found!")
            return@get
        }

        if (user.role != Constants.TEACHER_ROLE) {
            call.respond(HttpStatusCode.Conflict, "User is not a teacher!")
        }

        call.respond(HttpStatusCode.OK, "User is a teacher")
    }
}

fun Route.deleteUser(
    userDataSource: UserDataSource
) {
    authenticate {
        post("deleteUser") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class)?: kotlin.run{
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val user = userDataSource.getUserByUsername(userId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            val res = userDataSource.deleteUser(userId);

            if (!res) {
                call.respond(HttpStatusCode.Conflict, "Unable to delete user!")
                return@post
            }

            call.respond(HttpStatusCode.OK, "Successfully deleted!")
        }
    }
}

fun Route.changeRole(
    userDataSource: UserDataSource
) {
    authenticate{
        post("changeRole") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class)?: kotlin.run{
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val user = userDataSource.getUserByUsername(userId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            var newRole = Constants.STUDENT_ROLE
            if (user.role == Constants.STUDENT_ROLE) { newRole = Constants.TEACHER_ROLE }

            val res = userDataSource.changeRole(userId, newRole)

            if (!res) {
                call.respond(HttpStatusCode.Conflict, "Could not change user role")
                return@post
            }

            call.respond(HttpStatusCode.OK, "Role changed to $newRole")
            return@post
        }
    }
}

fun Route.changeFirstName(
    userDataSource: UserDataSource
) {
    authenticate{
        post("changeFirstName") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<ChangeNameRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            val res = userDataSource.changeFirstName(userId, request.newFirstName)
            if (!res) {
                call.respond(HttpStatusCode.Conflict, "Could not change user's firstname!")
                return@post
            }

            call.respond(HttpStatusCode.OK, "FirstName changed to ${request.newFirstName}")
            return@post
        }
    }
}

fun Route.changeLastName(
    userDataSource: UserDataSource
) {
    authenticate {
        post("changeLastName") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<ChangeNameRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            val res = userDataSource.changeLastName(userId, request.newFirstName)
            if (!res) {
                call.respond(HttpStatusCode.Conflict, "Could not change user's lastname!")
                return@post
            }

            call.respond(HttpStatusCode.OK, "LastName changed to ${request.newFirstName}")
            return@post
        }
    }
}

fun Route.changeFirstAndLastName(
    userDataSource: UserDataSource
) {
    authenticate{
        post("changeFirstAndLastName") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<ChangeNameRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            val res = userDataSource.changeFirstAndLastName(userId, request.newFirstName, request.newLastName)
            if (!res) {
                call.respond(HttpStatusCode.Conflict, "Could not change user's firstname and lastname!")
                return@post
            }

            call.respond(HttpStatusCode.OK, "FullName changed to ${request.newFirstName} ${request.newLastName}")
            return@post
        }
    }
}

fun Route.changeUsername(
    userDataSource: UserDataSource
) {
    authenticate {
        post("changeUsername") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<ChangeUsernameRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            val res = userDataSource.changeUsername(userId, request.newUsername)
            if (!res) {
                call.respond(HttpStatusCode.Conflict, "Could not change username")
                return@post
            }

            call.respond(HttpStatusCode.OK, "Username changed to ${request.newUsername}")
            return@post
        }
    }
}

fun Route.getClassSections(
    userDataSource: UserDataSource,
    lectureDataSource: LectureDataSource
) {
    authenticate {
        get("getClassSections") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "UserId not retrievable!");
                return@get
            }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@get
            }

            val classSectionObjsList = lectureDataSource.getLectures(user.classSectionList)

            val classSectionRespList = classSectionObjsList.map {
                c -> ClassSectionResponse(
                    c.id.toString(),
                    c.name,
                    c.teacherId.toString(),
                    c.studentIds.map { it.toString() },
                    c.isActive,
                    c.quizIds.map { it.toString() },
                    c.joinCode,
                    c.isJoinable
                )
            }

            call.respond(
                status = HttpStatusCode.OK,
                message = ClassSectionListResponse(classSectionRespList)
            )
        }
    }
}

fun Route.getClassSectionJoinableStatus(
    lectureDataSource: LectureDataSource
) {
    get("getClassSectionJoinableStatus") {
        val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
            return@get
        }

        val classSection = lectureDataSource.getLectureByID(request.classSectionId)?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "ClassSection not found!")
            return@get
        }

        call.respond(
            HttpStatusCode.OK,
            message = classSection.isJoinable
        )
    }
}

fun Route.getClassSectionJoinCode(
    lectureDataSource: LectureDataSource,
    userDataSource: UserDataSource
) {
    authenticate{
        get("getClassSectionJoinCode") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@get
            }

            val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@get
            }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@get
            }

            val classSection = lectureDataSource.getLectureByID(request.classSectionId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@get
            }

            if (classSection.teacherId.toString() != userId) {
                call.respond(HttpStatusCode.Conflict, "Caller is not the teacher for this class!")
                return@get
            }

            call.respond(
                HttpStatusCode.OK,
                message = classSection.joinCode
            )
        }
    }
}

fun Route.makeClassSectionJoinable(
    lectureDataSource: LectureDataSource,
    userDataSource: UserDataSource
){
    authenticate{
        post("makeClassSectionJoinable") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            val classSection = lectureDataSource.getLectureByID(request.classSectionId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@post
            }

            if (classSection.teacherId.toString() != userId) {
                call.respond(HttpStatusCode.Conflict, "Caller is not the teacher for this class!")
                return@post
            }

            val res = lectureDataSource.makeClassSectionJoinable(request.classSectionId)
            if (!res) {
                call.respond(HttpStatusCode.Conflict, message = "Unable to make ClassSection joinable!")
                return@post
            }

            call.respond(HttpStatusCode.OK, message = "ClassSection is now joinable!" )
            return@post
        }
    }
}

fun Route.makeClassSectionUnjoinable(
    lectureDataSource: LectureDataSource,
    userDataSource: UserDataSource
) {
    authenticate{
        post("makeClassSectionUnjoinable") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            val classSection = lectureDataSource.getLectureByID(request.classSectionId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@post
            }

            if (classSection.teacherId.toString() != userId) {
                call.respond(HttpStatusCode.Conflict, "Caller is not the teacher for this class!")
                return@post
            }

            val res = lectureDataSource.makeClassSectionUnjoinable(request.classSectionId)
            if (!res) {
                call.respond(HttpStatusCode.Conflict, message = "Unable to make ClassSection unjoinable!")
                return@post
            }

            call.respond(HttpStatusCode.OK, message = "ClassSection is now unjoinable!" )
            return@post
        }
    }
}

fun Route.joinClassSection(
    userDataSource: UserDataSource,
    lectureDataSource: LectureDataSource
) {
    authenticate{
        post("joinClassSection") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<JoinClassSectionRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            val classSection = lectureDataSource.getLectureByID(request.classSectionId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@post
            }

            if (user.role != Constants.STUDENT_ROLE) {
                call.respond(HttpStatusCode.Conflict, "User must be a Student!")
                return@post
            }

            if (!classSection.isJoinable) {
                call.respond(HttpStatusCode.Conflict, "Class not Joinable!")
                return@post
            }

            if (classSection.joinCode != request.classSectionJoinCode) {
                call.respond(HttpStatusCode.Conflict, "Invalid Join Code!")
                return@post
            }

            var studentInClass = false;
            for (classSectionId in user.classSectionList) {
                if (classSectionId.toString() == request.classSectionId) {
                    studentInClass = true
                    break
                }
            }

            if (studentInClass) {
                call.respond(HttpStatusCode.Conflict, "User already enrolled in this Class!")
                return@post
            }

            val classSectionAdd = lectureDataSource.addStudentToClassSection(request.classSectionId, userId)
            if (!classSectionAdd) {
                call.respond(HttpStatusCode.Conflict, "Couldn't add student to class!")
                return@post
            }

            val studentAdd = userDataSource.addClassSectionToStudent(userId, request.classSectionId)
            if (!studentAdd) {
                call.respond(HttpStatusCode.Conflict, "Couldn't add class to student!")
                return@post
            }

            call.respond(HttpStatusCode.OK, message = "Added student to class!")
            return@post
        }
    }
}

