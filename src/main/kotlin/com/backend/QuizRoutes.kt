package com.backend

import Quiz
import com.backend.data.Constants
import com.backend.data.quiz.QuizDataSource
import com.backend.data.requests.QuizRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.createQuiz(quizDataSource: QuizDataSource) {
    post("createquiz") {
        val request = kotlin.runCatching { call.receiveNullable<QuizRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        if (request.state != Constants.HIDDEN && request.state != Constants.FINISHED
            && request.state != Constants.CLOSED && request.state != Constants.OPEN) {
            call.respond(HttpStatusCode.Conflict,
                "State should be one of HIDDEN, CLOSED, FINISHED or OPEN, given state is ${request.state}")
            return@post;
        }

        // check that question id exists

        val quiz = Quiz(
            name = request.name,
            state = request.state,
            questions = request.questions
        );
        // Try to insert new user into DB
        val wasAcknowledged = quizDataSource.createQuiz(quiz);

        if (!wasAcknowledged) { // Error inserting new user into DB
            call.respond(HttpStatusCode.Conflict, "Unable to create quiz. Database Error.");
            return@post
        }

        call.respond(HttpStatusCode.OK, "Quiz Created!");

    }
}
