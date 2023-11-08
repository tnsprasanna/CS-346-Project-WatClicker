package com.backend.routes

import Quiz
import com.backend.data.Constants
import com.backend.data.lecture.LectureDataSource
import com.backend.data.questions.QuestionDataSource
import com.backend.data.quiz.QuizDataSource
import com.backend.data.requests.*
import com.backend.data.requests.GetQuizQuestionIdsRequest
import com.backend.data.responses.*
import com.backend.data.user.UserDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId
import java.lang.constant.ConstantDescs.NULL
import java.util.UUID
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Route.createQuiz(
    userDataSource: UserDataSource,
    lectureDataSource: LectureDataSource,
    quizDataSource: QuizDataSource,
) {
    authenticate {
        post("createQuiz") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class)?: kotlin.run{
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<CreateQuizRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserByUsername(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            val classSection = lectureDataSource.getLectureByID(request.classSectionId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@post
            }

            if (user.role != Constants.TEACHER_ROLE) {
                call.respond(HttpStatusCode.Conflict, "User must be a Teacher!")
                return@post
            }

            if (classSection.teacherId.toString() != userId) {
                call.respond(HttpStatusCode.Conflict, "User is not the teacher of this ClassSection!")
                return@post
            }

            if (!Constants.QUIZ_STATES.contains(request.state)) {
                call.respond(HttpStatusCode.Conflict, "Quiz state is invalid!")
                return@post
            }

            val quiz = Quiz(
                name = request.name,
                state = request.state,
                classSectionId = ObjectId(request.classSectionId),
                questionIds = mutableListOf<ObjectId>()
            )

            val res = quizDataSource.insertQuiz(quiz)
            if (!res) {
                call.respond(HttpStatusCode.Conflict, "Unable to create quiz! Database Error.");
                return@post
            }

            call.respond(HttpStatusCode.OK, "Quiz Created!");
        }
    }
}

fun Route.getQuizById(
    quizDataSource: QuizDataSource,
) {
    get("getQuizById") {
        val request = kotlin.runCatching { call.receiveNullable<QuizIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
            return@get
        }

        val quiz = quizDataSource.getQuizById(request.quizId)?: kotlin.run {
            call.respond(HttpStatusCode.Conflict, "Quiz not found!")
            return@get
        }

        val quizResponse = QuizResponse(
            id = quiz.id.toString(),
            name = quiz.name,
            state = quiz.state,
            classSectionId = quiz.classSectionId.toString(),
            questionIds = quiz.questionIds.map { it.toString() }
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = quizResponse
        )
    }
}

fun Route.changeQuizState(
    userDataSource: UserDataSource,
    lectureDataSource: LectureDataSource,
    quizDataSource: QuizDataSource
) {
    authenticate {
        post("changeQuizState") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class)?: kotlin.run{
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<ChangeStateRequest>() }.getOrNull() ?: kotlin.run {
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

            if (!Constants.QUIZ_STATES.contains(request.newState)) {
                call.respond(HttpStatusCode.Conflict, "Quiz newState is invalid!")
                return@post
            }

            val res = quizDataSource.changeQuizState(request.quizId, request.newState)
            if (!res) {
                call.respond(HttpStatusCode.Conflict, "Unable change the quiz state!");
                return@post
            }

            call.respond(HttpStatusCode.OK, "Quiz State Changed!");
        }
    }
}

fun Route.deleteQuiz(
    userDataSource: UserDataSource,
    lectureDataSource: LectureDataSource,
    quizDataSource: QuizDataSource
) {
    authenticate {
        post("changeQuizState") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class)?: kotlin.run{
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<QuizIdRequest>() }.getOrNull() ?: kotlin.run {
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

            val res = quizDataSource.deleteQuiz(request.quizId)

            if (!res) {
                call.respond(HttpStatusCode.Conflict, "Unable to delete quiz!");
                return@post
            }

            call.respond(HttpStatusCode.OK, "Quiz State Changed!");
        }
    }
}

fun Route.getQuizQuestions(
    quizDataSource: QuizDataSource,
    questionDataSource: QuestionDataSource
) {
    get("getQuizQuestions") {
        val request = kotlin.runCatching { call.receiveNullable<QuizIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val quiz = quizDataSource.getQuizById(request.quizId)?: kotlin.run {
            call.respond(HttpStatusCode.Conflict, "Quiz not found!")
            return@get
        }

        val quizQuestionObjsList = quizDataSource.getQuizQuestions(request.quizId).filterNotNull();

        val quizQuestionsRespList = quizQuestionObjsList.map {
            q -> QuestionResponse(
                id = q.id.toString(),
                question = q.question,
                options = q.options,
                responses = q.responses,
                answer = q.answer,
                selections = q.selections.map{ it.toString() },
            )
        }

        call.respond(
            status = HttpStatusCode.OK,
            message = QuestionListResponse(quizQuestionsRespList)
        )
    }
}