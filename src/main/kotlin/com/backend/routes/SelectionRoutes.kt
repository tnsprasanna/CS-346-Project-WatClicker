package com.backend.routes

import Quiz
import com.backend.data.Constants
import com.backend.data.questions.QuestionDataSource
import com.backend.data.quiz.QuizDataSource
import com.backend.data.requests.*
import com.backend.data.requests.GetQuizQuestionIdsRequest
import com.backend.data.responses.*
import com.backend.data.selection.Selection
import com.backend.data.selection.SelectionDataSource
import com.backend.data.user.UserDataSource
import com.backend.routes.authenticate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId
import java.lang.constant.ConstantDescs.NULL
import java.util.UUID


fun Route.getSelectionById(
    selectionDataSource: SelectionDataSource
) {
    get("getSelectionById") {
        val request = kotlin.runCatching { call.receiveNullable<SelectionIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
            return@get
        }

        val selection = selectionDataSource.getSelectionById(request.selectionId)?: kotlin.run {
            call.respond(HttpStatusCode.Conflict, "Selection not found!")
            return@get
        }

        call.respond(
            status = HttpStatusCode.OK,
            message = SelectionResponse(
                id = selection.id.toString(),
                questionId = selection.questionId.toString(),
                studentId = selection.studentId.toString(),
                selectedOption = selection.selectedOption,
                isCorrect = selection.isCorrect
            )
        )
    }
}


fun Route.createSelection(
    selectionDataSource: SelectionDataSource,
    userDataSource: UserDataSource,
    questionDataSource: QuestionDataSource
) {
    authenticate {
        post("createSelection") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<CreateSelectionRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            if (user.role != Constants.STUDENT_ROLE) {
                call.respond(HttpStatusCode.Conflict, "User must be a student!")
                return@post
            }

            val question = questionDataSource.getQuestionById(request.questionId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "Question not found!")
                return@post
            }

            if (request.selectedOption >= question.options.size) {
                call.respond(HttpStatusCode.Conflict, "Invalid SelectedOption!")
                return@post
            }

            val selection = Selection(
                questionId = question.id,
                studentId = user.id,
                selectedOption = request.selectedOption,
                isCorrect = question.answer == request.selectedOption
            )

            val res1 = selectionDataSource.createSelection(selection)
            if (!res1) {
                call.respond(HttpStatusCode.Conflict, "Unable to Create Selection! Database Error")
                return@post
            }

            val res2 = questionDataSource.addStat(request.questionId, request.selectedOption)?: kotlin.run{
                call.respond(HttpStatusCode.Conflict, "Database error!")
                return@post
            }
            if (!res2) {
                call.respond(HttpStatusCode.Conflict, "Unable to Create Selection (update stat)! Database Error")
                return@post
            }

            call.respond(HttpStatusCode.OK, "Selection Created!")
        }
    }
}

fun Route.deleteSelection(
    selectionDataSource: SelectionDataSource,
    userDataSource: UserDataSource,
    questionDataSource: QuestionDataSource
) {
    authenticate {
        post("deleteSelection") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request =
                kotlin.runCatching { call.receiveNullable<SelectionIdRequest>() }.getOrNull() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                    return@post
                }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            if (user.role != Constants.STUDENT_ROLE) {
                call.respond(HttpStatusCode.Conflict, "User must be a student!")
                return@post
            }

            val selection = selectionDataSource.getSelectionById(request.selectionId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "Selection not found!")
                return@post
            }

            if (user.id != selection.studentId) {
                call.respond(HttpStatusCode.Conflict, "Caller does not own this selection!")
                return@post
            }

            val res1 = selectionDataSource.deleteSelection(request.selectionId)?: kotlin.run{
                call.respond(HttpStatusCode.Conflict, "Database error1!")
                return@post
            }
            val res2 = questionDataSource.removeStat(selection.questionId.toString(), selection.selectedOption)?:kotlin.run{
                call.respond(HttpStatusCode.Conflict, "Database error2!")
                return@post
            }

            if (!res1 || !res2) {
                call.respond(HttpStatusCode.Conflict, "Could not remove selection!")
                return@post
            }

            call.respond(HttpStatusCode.OK, "Selection Removed!")
            return@post
        }
    }
}

fun Route.editSelection(
    selectionDataSource: SelectionDataSource,
    userDataSource: UserDataSource,
    questionDataSource: QuestionDataSource
) {
    authenticate {
        post("editSelection") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request =
                kotlin.runCatching { call.receiveNullable<EditSelectionRequest>() }.getOrNull() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                    return@post
                }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            if (user.role != Constants.STUDENT_ROLE) {
                call.respond(HttpStatusCode.Conflict, "User must be a student!")
                return@post
            }

            val selection = selectionDataSource.getSelectionById(request.selectionId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "Selection not found!")
                return@post
            }

            if (user.id != selection.studentId) {
                call.respond(HttpStatusCode.Conflict, "Caller does not own this selection!")
                return@post
            }

            val question = questionDataSource.getQuestionById(selection.questionId.toString()) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "Question not found!")
                return@post
            }

            val res1 = questionDataSource.changeStat(question.id.toString(), selection.selectedOption, request.newOption)
            val res2 = selectionDataSource.editSelection(request.selectionId, request.newOption, question.answer == request.newOption) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "Could not edit selection! DataBase Error 1")
                return@post
            }

            if (!res1 || !res2) {
                call.respond(HttpStatusCode.Conflict, "Could not edit selection!")
                return@post
            }

            call.respond(HttpStatusCode.OK, "Selection Edited!")
            return@post
        }
    }
}


/*

fun Route.editSelection(selectionDataSource: SelectionDataSource, questionDataSource: QuestionDataSource) {
    patch("editSelection") {
        val request = kotlin.runCatching { call.receiveNullable<EditSelectionRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@patch
        }
        val question = questionDataSource.getQuestionById(request.questionId);
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


 */