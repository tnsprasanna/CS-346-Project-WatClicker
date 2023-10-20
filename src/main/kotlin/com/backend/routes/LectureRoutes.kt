package com.backend.routes

import Lecture
import com.backend.data.Constants
import com.backend.data.requests.LectureRequest
import com.backend.data.lecture.LectureDataSource
import com.backend.security.hashing.HashingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.backend.data.responses.AuthResponse

fun Route.createLecture(
    lectureDataSource: LectureDataSource,

) {
    post("createLecture") {
        val request = kotlin.runCatching { call.receiveNullable<LectureRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val areFieldsBlank = request.name.isBlank();

        val existingUser = lectureDataSource.getLectureByName(request.name)

        if (areFieldsBlank) { // All user fields must be filled in
            call.respond(HttpStatusCode.Conflict, "Some fields are blank!");
            return@post
        }
        else if (existingUser != null) {
            call.respond(HttpStatusCode.Conflict, "Lecture exists.");
            return@post
        }
        val lecture = Lecture(
            name = request.name,
            active = request.active,

        );

        val wasAcknowledged = lectureDataSource.addLecture(lecture)

        if (!wasAcknowledged) { // Error inserting new user into DB
            call.respond(HttpStatusCode.Conflict, "Unable to create lecture! Database Error.");
            return@post
        }

        call.respond(HttpStatusCode.OK, "Lecture Created!");
    }
}

fun Route.getQuizFromLecture() {
        get("getQuizFromLecture") {
            call.respond(HttpStatusCode.OK)
        }
    }