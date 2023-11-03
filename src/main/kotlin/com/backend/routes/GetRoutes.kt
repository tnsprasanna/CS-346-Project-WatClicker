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

fun Route.getUsers(
    userDataSource: UserDataSource
) {
    get("getUsers") {
        val userResponses = userDataSource.getUsers().map {
            u -> UserResponse(u.id.toString(),
                                u.username,
                                u.role,
                                u.firstname,
                                u.lastname,
                                u.classSectionList.map { it.toString() })
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
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val user = userDataSource.getUserById(request.userId);

        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "User not found!")
            return@get
        }

        call.respond(
            status = HttpStatusCode.OK,
            message = UserResponse(user.id.toString(),
                user.username,
                user.role,
                user.firstname,
                user.lastname,
                user.classSectionList.map { it.toString() })
        )
    }
}

fun Route.getTeachers(
    userDataSource: UserDataSource
) {
    get("getTeachers") {
        val teacherResponses = userDataSource.getTeachers().map {
                u -> UserResponse(u.id.toString(),
            u.username,
            u.role,
            u.firstname,
            u.lastname,
            u.classSectionList.map { it.toString() })
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
                u -> UserResponse(u.id.toString(),
            u.username,
            u.role,
            u.firstname,
            u.lastname,
            u.classSectionList.map { it.toString() })
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
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val user = userDataSource.getUserById(request.userId);

        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "User not found!")
            return@get
        }

        if (user.role != Constants.STUDENT_ROLE) {
            call.respond(HttpStatusCode.Conflict, "User is not a Student!")
        }

        call.respond(HttpStatusCode.OK, "User is a Student")
    }
}

fun Route.isStudentFromUsername(
    userDataSource: UserDataSource
) {
    get("isStudentFromUsername") {
        val request = kotlin.runCatching { call.receiveNullable<UsernameRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val user = userDataSource.getUserByUsername(request.username);

        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "User not found!")
            return@get
        }

        if (user.role != Constants.STUDENT_ROLE) {
            call.respond(HttpStatusCode.Conflict, "User is not a Student!")
        }

        call.respond(HttpStatusCode.OK, "User is a Student")
    }
}

fun Route.isTeacherFromId(
    userDataSource: UserDataSource
) {
    get("isTeacherFromId") {
        val request = kotlin.runCatching { call.receiveNullable<UserIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val user = userDataSource.getUserById(request.userId);

        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "User not found!")
            return@get
        }

        if (user.role != Constants.TEACHER_ROLE) {
            call.respond(HttpStatusCode.Conflict, "User is not a Teacher!")
        }

        call.respond(HttpStatusCode.OK, "User is a Teacher")
    }
}

fun Route.isTeacherFromUsername(
    userDataSource: UserDataSource
) {
    get("isTeacherFromUsername") {
        val request = kotlin.runCatching { call.receiveNullable<UsernameRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val user = userDataSource.getUserByUsername(request.username);

        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "User not found!")
            return@get
        }

        if (user.role != Constants.TEACHER_ROLE) {
            call.respond(HttpStatusCode.Conflict, "User is not a Teacher!")
        }

        call.respond(HttpStatusCode.OK, "User is a Teacher")
    }
}

fun Route.deleteUser(
    userDataSource: UserDataSource
) {
    post("deleteUser") {
        call.respond(HttpStatusCode.NoContent, "Not yet implemented")
    }
}

fun Route.changeRole(
    userDataSource: UserDataSource
) {
    post("changeRole") {
        call.respond(HttpStatusCode.NoContent, "Not yet implemented")
    }
}

fun Route.changeFirstName(
    userDataSource: UserDataSource
) {
    post("changeFirstName") {
        call.respond(HttpStatusCode.NoContent, "Not yet implemented")
    }
}

fun Route.changeLastName(
    userDataSource: UserDataSource
) {
    post("changeLastName") {
        call.respond(HttpStatusCode.NoContent, "Not yet implemented")
    }
}

fun Route.changeFirstAndLastName(
    userDataSource: UserDataSource
) {
    post("changeFirstAndLastName") {
        call.respond(HttpStatusCode.NoContent, "Not yet implemented")
    }
}

fun Route.changeUsername(
    userDataSource: UserDataSource
) {
    post("changeUsername") {
        call.respond(HttpStatusCode.NoContent, "Not yet implemented")
    }
}




/*




























 */












fun Route.getClassSections(
    userDataSource: UserDataSource,
    lectureDataSource: LectureDataSource
) {
    get("getClassSections") {
        val request = kotlin.runCatching { call.receiveNullable<UserIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Couldn't parse params!")
            return@get
        }

        val user = userDataSource.getUserById(request.userId)?: kotlin.run {
            call.respond(HttpStatusCode.Conflict, "user not found")
            return@get
        }

        val classSectionObjsList = lectureDataSource.getLectures(user.classSectionList)

        val classSectionRespList = classSectionObjsList.map {c -> ClassSectionResponse(
            c.id.toString(),
            c.name,
            c.teacherId.toString(),
            c.studentIds.map { it.toString() },
            c.isActive,
            c.quizIds.map { it.toString() },
            c.joinCode,
            c.isJoinable
        )}

        call.respond(
            status = HttpStatusCode.OK,
            message = ClassSectionListResponse(classSectionRespList)
        )
    }
}

fun Route.getClassSectionJoinableStatus( // READ FROM CLASS OBJECT
    lectureDataSource: LectureDataSource
) {
    get("getClassSectionJoinableStatus") {
        call.respond(HttpStatusCode.NoContent, "Not yet implemented")
        val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Couldn't parse params!")
            return@get
        }

        val classSection = lectureDataSource.getLectureByID(request.classSectionId)?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Class section does not exist")
            return@get
        }

        call.respond(
            HttpStatusCode.OK,
            message = classSection.isJoinable
        )
    }
}

fun Route.getClassSectionJoinCode( // READ FROM CLASS OBJECT
    lectureDataSource: LectureDataSource
) {
    get("getClassSectionJoinCode") {
        val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Couldn't parse params!")
            return@get
        }

        val classSection = lectureDataSource.getLectureByID(request.classSectionId)?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Class section does not exist")
            return@get
        }

        call.respond(
            HttpStatusCode.OK,
            message = classSection.joinCode
        )
    }
}

fun Route.makeClassSectionJoinable(
    lectureDataSource: LectureDataSource
){
    post("makeClassSectionJoinable") {
        val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Couldn't parse params!")
            return@post
        }

        val classSection = lectureDataSource.getLectureByID(request.classSectionId)?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Class section does not exist")
            return@post
        }

        val resp = lectureDataSource.makeClassSectionJoinable(request.classSectionId)
        if (!resp) {
            call.respond(
                HttpStatusCode.Conflict,
                message = "Something Went Wrong"
            )
        }

        call.respond(
            HttpStatusCode.OK,
            message = "Class is joinable!"
        )
    }
}

fun Route.makeClassSectionUnjoinable(
    lectureDataSource: LectureDataSource
) {
    post("makeClassSectionJoinable") {
        val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Couldn't parse params!")
            return@post
        }

        val classSection = lectureDataSource.getLectureByID(request.classSectionId)?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Class section does not exist")
            return@post
        }

        val resp = lectureDataSource.makeClassSectionUnjoinable(request.classSectionId)
        if (!resp) {
            call.respond(
                HttpStatusCode.Conflict,
                message = "Something Went Wrong"
            )
        }

        call.respond(
            HttpStatusCode.OK,
            message = "Class is joinable!"
        )
    }
}

fun Route.joinClassSection(
    userDataSource: UserDataSource,
    lectureDataSource: LectureDataSource
) {
    post("joinClassSection") {
        val request = kotlin.runCatching { call.receiveNullable<JoinClassSectionRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Couldn't parse params!")
            return@post
        }

        val user = userDataSource.getUserById(request.userId)?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "User Doesn't Exist!")
            return@post
        }

        if (user.role != Constants.STUDENT_ROLE) {
            call.respond(HttpStatusCode.BadRequest, "User must be a Student!")
            return@post
        }

        val classSection = lectureDataSource.getLectureByID(request.classSectionId)?: kotlin.run{
            call.respond(HttpStatusCode.BadRequest, "Class not found!")
            return@post
        }

        if (!classSection.isJoinable) {
            call.respond(HttpStatusCode.BadRequest, "Class not Joinable!")
            return@post
        }

        if (classSection.joinCode != request.classSectionJoinCode) {
            call.respond(HttpStatusCode.BadRequest, "Invalid Join Code!")
            return@post
        }

        val classSectionAdd = lectureDataSource.addStudentToClassSection(request.classSectionId, request.userId)
        if (!classSectionAdd) {
            call.respond(HttpStatusCode.BadRequest, "Couldn't add student to class!")
            return@post
        }

        val studentAdd = userDataSource.addClassSectionToStudent(request.userId, request.classSectionId)
        if (!studentAdd) {
            call.respond(HttpStatusCode.BadRequest, "Couldn't add class to student!")
            return@post
        }

        call.respond(
            HttpStatusCode.OK,
            message = "Added student to class!"
        )
    }
}

