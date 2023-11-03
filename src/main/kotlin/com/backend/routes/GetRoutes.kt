package com.backend.routes

import User
import com.backend.data.user.UserDataSource
import com.backend.data.Constants
import com.backend.data.lecture.LectureDataSource
import com.backend.data.requests.SignInRequest
import com.backend.data.requests.UserIdRequest
import com.backend.data.requests.UsernameRequest
import com.backend.data.responses.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.codecs.pojo.annotations.BsonId

fun Route.getClassSections() {
    get("getClassSections") {
        call.respond(HttpStatusCode.OK, "Get Class Sections")
    }
}

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

//        val classSectionObjsList = user.classSectionList.map {
//            lectureDataSource.getClassSectionById(it)
//        };
//
//        val classSectionRespList = classSectionObjsList.map {c -> ClassSectionResponse(
//            c.id.toString(),
//            c.name,
//            c.teacherId.toString(),
//            c.studentIds.map { it.toString() },
//            c.isActive,
//            c.quizIds.map { it.toString() },
//            c.joinCode,
//            c.isJoinable
//        )}
//
//        call.respond(
//            status = HttpStatusCode.OK,
//            message = ClassSectionListResponse(classSectionRespList)
//        )
    }
}

fun Route.getClassSectionJoinableStatus( // READ FROM CLASS OBJECT
    lectureDataSource: LectureDataSource
) {
    get("getClassSectionJoinableStatus") {
        call.respond(HttpStatusCode.NoContent, "Not yet implemented")
    }
}

fun Route.getClassSectionJoinCode( // READ FROM CLASS OBJECT
    lectureDataSource: LectureDataSource
) {
    get("getClassSectionJoinCode") {
        call.respond(HttpStatusCode.NoContent, "Not yet implemented")
    }
}

fun Route.makeClassSectionJoinable(
    lectureDataSource: LectureDataSource
){
    post("makeClassSectionJoinable") {
        call.respond(HttpStatusCode.NoContent, "Not yet implemented")
    }
}

fun Route.makeClassSectionUnjoinable(
    lectureDataSource: LectureDataSource
) {
    post("makeClassSectionJoinable") {
        call.respond(HttpStatusCode.NoContent, "Not yet implemented")
    }
}

fun Route.joinClassSection(
    userDataSource: UserDataSource,
    lectureDataSource: LectureDataSource
) {
    post("joinClassSection") {
        call.respond(HttpStatusCode.NoContent, "Not yet implemented")
    }
}

