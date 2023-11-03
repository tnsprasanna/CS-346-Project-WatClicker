package com.backend.routes

import Quiz
import Selection
import com.backend.data.Constants
import com.backend.data.questions.QuestionDataSource
import com.backend.data.quiz.QuizDataSource
import com.backend.data.requests.*
import com.backend.data.requests.GetQuizQuestionIdsRequest
import com.backend.data.responses.*
import com.backend.data.selection.SelectionDataSource
import com.backend.data.user.UserDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId
import java.lang.constant.ConstantDescs.NULL
import java.util.UUID

fun Route.createSelection(selectionDataSource: SelectionDataSource, questionDataSource: QuestionDataSource, userDataSource: UserDataSource) {
    post("createSelection") {
        val request = kotlin.runCatching { call.receiveNullable<CreateSelectionRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val question = questionDataSource.getQuestion(request.questionId);
        val optionsLen = question?.options?.size;
        if (question == null) {
            call.respond(HttpStatusCode.Conflict, "Invalid question selected.")
            return@post
        }

        if (userDataSource.getUserById(request.studentId) == null) {
            call.respond(HttpStatusCode.Conflict, "Invalid student selected.")
            return@post
        }

        if (request.selectedOption > optionsLen!! || request.selectedOption < 0) {
            call.respond(HttpStatusCode.Conflict, "Invalid option selected.")
            return@post
        }

        val selectionId =  UUID.randomUUID().toString();
        val selection = Selection(
            selectionId = selectionId,
            questionId = request.questionId,
            studentId = request.studentId,
            selectedOption = request.selectedOption,
            isCorrect = request.selectedOption == question.answer
        );
        // Try to insert new user into DB
        val wasAcknowledged = selectionDataSource.createSelection(selection);

        if (!wasAcknowledged) { // Error inserting new user into DB
            call.respond(HttpStatusCode.Conflict, "Unable to create selection. Database Error.");
            return@post
        }

        call.respond(HttpStatusCode.OK, "${selectionId} Selection Created!");

        }
}

fun Route.deleteSelection(selectionDataSource: SelectionDataSource) {
    delete("deleteSelection") {
        val request = kotlin.runCatching { call.receiveNullable<DeleteSelectionRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@delete
        }
        val wasAcknowledged = selectionDataSource.deleteSelection(request.selectionId);

        if (!wasAcknowledged) { // Error inserting new user into DB
            call.respond(HttpStatusCode.Conflict, "Unable to delete selection. Database Error.");
            return@delete
        }
        call.respond(HttpStatusCode.OK, "Selection ${request.selectionId} deleted!")

    }
}

fun Route.getSelectionById(selectionDataSource: SelectionDataSource) {
    get("getSelectionById") {
        val request = kotlin.runCatching { call.receiveNullable<GetSelectionByIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val selection = selectionDataSource.getSelectionById(request.selectionId);

        if (selection == null) { // Error inserting new user into DB
            call.respond(HttpStatusCode.Conflict, "Selection doesn't exist.");
            return@get
        }

        val selectionResponse = Selection(
            selectionId = selection.selectionId,
            questionId = selection.questionId,
            studentId = selection.studentId,
            selectedOption = selection.selectedOption,
            isCorrect = selection.isCorrect
        )

        call.respond(HttpStatusCode.OK, selectionResponse)

    }
}

fun Route.editSelection(selectionDataSource: SelectionDataSource, questionDataSource: QuestionDataSource) {
    patch("editSelection") {
        val request = kotlin.runCatching { call.receiveNullable<EditSelectionRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@patch
        }
        val question = questionDataSource.getQuestion(request.questionId);
        val len = question?.options?.size;

        if (question == null) {
            call.respond(HttpStatusCode.Conflict, "Invalid question selected.")
            return@patch
        }

        if (request.newOption > len!! || request.newOption < 0) {
            call.respond(HttpStatusCode.Conflict, "Invalid option selected.")
            return@patch
        }

        val wasAcknowledged = selectionDataSource.editSelection(request.selectionId, request.newOption,
            question.answer == request.newOption)

        if (!wasAcknowledged) { // Error inserting new user into DB
            call.respond(HttpStatusCode.Conflict, "Unable to edit selection. Database Error.");
            return@patch
        }

        call.respond(HttpStatusCode.OK, "Edited selection!");

    }
}