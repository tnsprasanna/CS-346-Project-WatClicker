package com.backend.routes

import User
import com.backend.data.user.UserDataSource
import com.backend.data.Constants
import com.backend.data.responses.AuthResponse
import com.backend.data.responses.UserListResponse
import com.backend.data.responses.UserResponse
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
            u -> UserResponse(u.id.toString(), u.username, u.role, u.firstname, u.lastname)
        };

       call.respond(
            status = HttpStatusCode.OK,
            message = UserListResponse(userResponses)
        )
    }
}

fun Route.getTeachers(
    userDataSource: UserDataSource
) {
    get("getTeachers") {
        val teacherResponses = userDataSource.getTeachers().map {
            u -> UserResponse(u.id.toString(), u.username, u.role, u.firstname, u.lastname)
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
            u -> UserResponse(u.id.toString(), u.username, u.role, u.firstname, u.lastname)
        };

        call.respond(
            status = HttpStatusCode.OK,
            message = UserListResponse(studentResponses)
        )
    }
}


