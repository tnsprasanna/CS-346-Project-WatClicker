package com.backend.routes

import com.backend.data.questions.Question
import com.backend.data.questions.QuestionDataSource
import com.backend.data.requests.AddSelectionToQuestionRequest
import com.backend.data.requests.GetQuestionRequest
import com.backend.data.requests.QuestionRequest
import com.backend.data.responses.QuestionResponse
import com.backend.data.selection.SelectionDataSource
import com.backend.security.hashing.HashingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.addQuestion(
    questionDataSource: QuestionDataSource,
) {
    post("addQuestion") {
        val request = kotlin.runCatching { call.receiveNullable<QuestionRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val questionId = UUID.randomUUID().toString()
        val question = Question(
            questionId = questionId,
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
        call.respond(HttpStatusCode.OK,questionId)
    }

}

fun Route.getQuestion(
    questionDataSource: QuestionDataSource,
) {
    get("getQuestion") {
        val request = kotlin.runCatching { call.receiveNullable<GetQuestionRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val questionId = request.questionId
        val selectedQuestion = questionDataSource.getQuestion(questionId)

        if (selectedQuestion == null) {
            call.respond(HttpStatusCode.BadRequest, "selectedq was null")
            return@get
        }
        val questionResponse =
            QuestionResponse(
                question = selectedQuestion.question,
                options = selectedQuestion.options,
                responses = selectedQuestion.responses,
                answer = selectedQuestion.answer,
            )

        call.respond(
            HttpStatusCode.OK,
            message = questionResponse
        )
    }
}
fun Route.deleteQuestion(
    questionDataSource: QuestionDataSource,
) {
    delete("deleteQuestion") {
        val request = kotlin.runCatching { call.receiveNullable<GetQuestionRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@delete
        }

        val questionId = request.questionId
        val deleteResult = questionDataSource.deleteQuestion(questionId)
        if (!deleteResult) {
            call.respond(HttpStatusCode.OK, "nothing was deleted.")
        }
        call.respond(HttpStatusCode.OK, "deletion successful")

    }
}

fun Route.addSelectionToQuestion(
    questionDataSource: QuestionDataSource,
    selectionDataSource: SelectionDataSource
) {
    patch("addSelectionToQuestion") {
        val request = kotlin.runCatching { call.receiveNullable<AddSelectionToQuestionRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@patch
        }

        if (questionDataSource.getQuestion(questionId = request.questionId) == null) {
            call.respond(HttpStatusCode.Conflict, "question does not exist")
            return@patch
        }

        if (selectionDataSource.getSelectionById(selectionId = request.selectionId) == null) {
            call.respond(HttpStatusCode.Conflict, "selection does not exist")
            return@patch
        }

        val wasAcknowledged = questionDataSource.addSelectionToQuestion(request.questionId, request.selectionId)
        if (!wasAcknowledged) {
            call.respond(HttpStatusCode.OK, "selection could not be added to question.")
        }
        call.respond(HttpStatusCode.OK, "Selection added to question!")

    }
}