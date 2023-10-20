package com.backend.routes

import com.backend.data.questions.Question
import com.backend.data.questions.QuestionDataSource
import com.backend.data.requests.QuestionRequest
import com.backend.security.hashing.HashingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.addQuestion(
    questionDataSource: QuestionDataSource,
    hashingService: HashingService
) {
    post("addQuestion") {
        val request = kotlin.runCatching { call.receiveNullable<QuestionRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val question = Question(
            question = request.question,
            options = request.options,
            responses = request.responses,
            answer = request.answer
        )
        val wasAcknowledged = questionDataSource.addQuestion(question)
        if (!wasAcknowledged) { // Error inserting new user into DB
            call.respond(HttpStatusCode.Conflict, "Unable to create user! Database Error.");
            return@post
        }
        call.respond(HttpStatusCode.OK,request.question)
    }

}