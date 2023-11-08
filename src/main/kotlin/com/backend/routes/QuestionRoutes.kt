package com.backend.routes

import com.backend.data.Constants
import com.backend.data.lecture.LectureDataSource
import com.backend.data.questions.Question
import com.backend.data.questions.QuestionDataSource
import com.backend.data.quiz.QuizDataSource
import com.backend.data.requests.*
import com.backend.data.responses.QuestionResponse
import com.backend.data.selection.SelectionDataSource
import com.backend.data.user.UserDataSource
import com.backend.security.hashing.HashingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.bson.types.ObjectId

fun Route.getQuestionById(
    questionDataSource: QuestionDataSource
) {
    get("getQuestionById") {
        val request = kotlin.runCatching { call.receiveNullable<QuestionIdRequest>() }.getOrNull()?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
            return@get
        }

        val question = questionDataSource.getQuestionById(request.questionId)?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Question not found!")
            return@get
        }

        val questionResponse = QuestionResponse(
            id = question.id.toString(),
            question = question.question,
            options = question.options,
            responses = question.responses,
            answer = question.answer,
            selections = question.selections.map { it.toString() }
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = questionResponse
        )
    }
}

fun Route.createQuestion(
    userDataSource: UserDataSource,
    lectureDataSource: LectureDataSource,
    quizDataSource: QuizDataSource,
    questionDataSource: QuestionDataSource
) {
    authenticate {
        post("createQuestion") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class)?: kotlin.run{
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<CreateQuestionRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            if (user.role != Constants.TEACHER_ROLE) {
                call.respond(HttpStatusCode.Conflict, "User must be a Teacher!")
                return@post
            }

            val quiz = quizDataSource.getQuizById(request.quizId)?: kotlin.run{
                call.respond(HttpStatusCode.Conflict, "Quiz not found!")
                return@post
            }

            val classSection = lectureDataSource.getLectureByID(quiz.classSectionId.toString())?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@post
            }

            if (classSection.teacherId.toString() != userId) {
                call.respond(HttpStatusCode.Conflict, "User is not the teacher of the class that this quiz is a part of!")
                return@post
            }

            if (!classSection.quizIds.contains(ObjectId(request.quizId))) {
                call.respond(HttpStatusCode.Conflict, "Quiz does not belong to the right class!")
                return@post
            }

            val question = Question(
                question = request.question,
                options =request.options.toMutableList(),
                responses = mutableListOf<Int>(),
                answer = request.answer,
                selections = mutableListOf<ObjectId>()
            )

            val res1 = questionDataSource.insertQuestion(question)
            if (!res1) {
                call.respond(HttpStatusCode.Conflict, "Unable to insert Question - Could not create!")
                return@post
            }

            val res2 = quizDataSource.addQuestionToQuiz(request.quizId, question.id.toString())
            if (!res2) {
                call.respond(HttpStatusCode.Conflict, "Unable to insert Question - Could not reference!")
                return@post
            }

            call.respond(HttpStatusCode.OK, "Created Question.")
            return@post
        }
    }
}

fun Route.deleteQuestion(
    userDataSource: UserDataSource,
    lectureDataSource: LectureDataSource,
    quizDataSource: QuizDataSource,
    questionDataSource: QuestionDataSource
) {
    authenticate {
        post("deleteQuestion") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class)?: kotlin.run{
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<DeleteQuestionRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            if (user.role != Constants.TEACHER_ROLE) {
                call.respond(HttpStatusCode.Conflict, "User must be a Teacher!")
                return@post
            }

            val quiz = quizDataSource.getQuizById(request.quizId)?: kotlin.run{
                call.respond(HttpStatusCode.Conflict, "Quiz not found!")
                return@post
            }

            val classSection = lectureDataSource.getLectureByID(quiz.classSectionId.toString())?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@post
            }

            if (classSection.teacherId.toString() != userId) {
                call.respond(HttpStatusCode.Conflict, "User is not the teacher of the class that this quiz is a part of!")
                return@post
            }

            if (!classSection.quizIds.contains(ObjectId(request.quizId))) {
                call.respond(HttpStatusCode.Conflict, "Quiz does not belong to the right class!")
                return@post
            }

            val res1 = questionDataSource.deleteQuestion(request.questionId)
            if (!res1) {
                call.respond(HttpStatusCode.Conflict, "Unable to delete question - Could not delete obj!")
                return@post
            }

            val res2 = quizDataSource.removeQuestionFromQuiz(request.quizId, request.questionId)
            if (!res2) {
                call.respond(HttpStatusCode.Conflict, "Unable to delete question - Could not delete reference!")
                return@post
            }

            call.respond(HttpStatusCode.OK, "Created Deleted.")
            return@post
        }
    }
}




//
//fun Route.addQuestion(
//    questionDataSource: QuestionDataSource,
//) {
//    post("addQuestion") {
//        val request = kotlin.runCatching { call.receiveNullable<QuestionRequest>() }.getOrNull() ?: kotlin.run {
//            call.respond(HttpStatusCode.BadRequest)
//            return@post
//        }
//        val questionId = UUID.randomUUID().toString()
//        val question = Question(
//            questionId = questionId,
//            question = request.question,
//            options = request.options,
//            responses = request.responses,
//            answer = request.answer
//        )
//        val wasAcknowledged = questionDataSource.addQuestion(question)
//        if (!wasAcknowledged) { // Error inserting new user into DB
//            call.respond(HttpStatusCode.Conflict, "Unable to create user! Database Error.");
//            return@post
//        }
//        call.respond(HttpStatusCode.OK,questionId)
//    }
//
//}
//
//fun Route.getQuestion(
//    questionDataSource: QuestionDataSource,
//) {
//    get("getQuestion") {
//        val request = kotlin.runCatching { call.receiveNullable<GetQuestionRequest>() }.getOrNull() ?: kotlin.run {
//            call.respond(HttpStatusCode.BadRequest)
//            return@get
//        }
//
//        val questionId = request.questionId
//        val selectedQuestion = questionDataSource.getQuestion(questionId)
//
//        if (selectedQuestion == null) {
//            call.respond(HttpStatusCode.BadRequest, "selectedq was null")
//            return@get
//        }
//        val questionResponse =
//            QuestionResponse(
//                question = selectedQuestion.question,
//                options = selectedQuestion.options,
//                responses = selectedQuestion.responses,
//                answer = selectedQuestion.answer,
//            )
//
//        call.respond(
//            HttpStatusCode.OK,
//            message = questionResponse
//        )
//    }
//}
//fun Route.deleteQuestion(
//    questionDataSource: QuestionDataSource,
//) {
//    delete("deleteQuestion") {
//        val request = kotlin.runCatching { call.receiveNullable<GetQuestionRequest>() }.getOrNull() ?: kotlin.run {
//            call.respond(HttpStatusCode.BadRequest)
//            return@delete
//        }
//
//        val questionId = request.questionId
//        val deleteResult = questionDataSource.deleteQuestion(questionId)
//        if (!deleteResult) {
//            call.respond(HttpStatusCode.OK, "nothing was deleted.")
//        }
//        call.respond(HttpStatusCode.OK, "deletion successful")
//
//    }
//}
//
//fun Route.addSelectionToQuestion(
//    questionDataSource: QuestionDataSource,
//    selectionDataSource: SelectionDataSource
//) {
//    patch("addSelectionToQuestion") {
//        val request = kotlin.runCatching { call.receiveNullable<AddSelectionToQuestionRequest>() }.getOrNull() ?: kotlin.run {
//            call.respond(HttpStatusCode.BadRequest)
//            return@patch
//        }
//
//        if (questionDataSource.getQuestion(questionId = request.questionId) == null) {
//            call.respond(HttpStatusCode.Conflict, "question does not exist")
//            return@patch
//        }
//
//        if (selectionDataSource.getSelectionById(selectionId = request.selectionId) == null) {
//            call.respond(HttpStatusCode.Conflict, "selection does not exist")
//            return@patch
//        }
//
//        val wasAcknowledged = questionDataSource.addSelectionToQuestion(request.questionId, request.selectionId)
//        if (!wasAcknowledged) {
//            call.respond(HttpStatusCode.OK, "selection could not be added to question.")
//        }
//        call.respond(HttpStatusCode.OK, "Selection added to question!")
//
//    }
//}