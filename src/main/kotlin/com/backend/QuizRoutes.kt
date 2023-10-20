package com.backend

import Quiz
import com.backend.data.Constants
import com.backend.data.quiz.QuizDataSource
import com.backend.data.requests.ChangeStateRequest
import com.backend.data.requests.DeleteQuizRequest
import com.backend.data.requests.GetQuestionsRequest
import com.backend.data.requests.QuizRequest
import com.backend.data.responses.ChangeStateResponse
import com.backend.data.responses.DeleteQuizResponse
import com.backend.data.responses.GetQuestionsResponse
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

        if (request.state !== Constants.HIDDEN && request.state !== Constants.FINISHED
            && request.state !== Constants.CLOSED && request.state !== Constants.OPEN) {
            call.respond(HttpStatusCode.Conflict, "State should be one of HIDDEN, CLOSED, FINISHED or OPEN")
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

fun Route.getQuestions(quizDataSource: QuizDataSource) { //maybe getQuestions
    get("getQuestions") {
        val request = kotlin.runCatching { call.receiveNullable<GetQuestionsRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val quiz = quizDataSource.getQuestions(request.quizId)

        if (quiz != null) {
            if (quiz.state == Constants.CLOSED || quiz.state == Constants.HIDDEN) {
                call.respond(HttpStatusCode.Conflict, "State should be OPEN or FINISHED for questions to be visible")
            }
        } else {
            call.respond(HttpStatusCode.Conflict, "Quiz is NULL")
        }

        if (quiz != null) {
            call.respond(HttpStatusCode.OK, GetQuestionsResponse(quiz.questions))
        }
    }
}

fun Route.changeState(quizDataSource: QuizDataSource) {
    patch("changeState") {
        val request = kotlin.runCatching { call.receiveNullable<ChangeStateRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@patch
        }

        val state = quizDataSource.changeState(request.quizId, request.newState)

        call.respond(HttpStatusCode.OK, ChangeStateResponse(true))
    }
}

    fun Route.deleteQuiz(quizDataSource: QuizDataSource) {
        delete("deleteQuiz") {
            val request = kotlin.runCatching { call.receiveNullable<DeleteQuizRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            val quiz = quizDataSource.deleteQuiz(request.quizId)

            call.respond(HttpStatusCode.OK, DeleteQuizResponse("Deletion was successful!"))

        }
    }
