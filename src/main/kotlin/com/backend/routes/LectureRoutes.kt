package com.backend.routes

import Lecture
import com.backend.data.Constants
import com.backend.data.requests.LectureRequest
import com.backend.data.requests.GetLectureByIDRequest
import com.backend.data.lecture.LectureDataSource
import com.backend.data.questions.QuestionDataSource
import com.backend.data.requests.DeleteLectureRequest
import com.backend.data.quiz.QuizDataSource
import com.backend.data.requests.DeleteQuizRequest
import com.backend.data.requests.GetQuestionRequest
import com.backend.security.hashing.HashingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.backend.data.responses.AuthResponse
import com.backend.data.responses.DeleteLectureResponse
import com.backend.data.responses.QuestionResponse
import org.bson.types.ObjectId

fun Route.createLecture(
    lectureDataSource: LectureDataSource,
    ) {
    post("createLecture") {
        //call.respond(HttpStatusCode.OK, "Lecture !")
        println("HI3!!!!!!!!!!!!!")

        val request = kotlin.runCatching { call.receiveNullable<LectureRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val areFieldsBlank = request.name.isBlank();

        //val existingUser = lectureDataSource.getLectureByName(request.name)

        if (areFieldsBlank) { // All user fields must be filled in
            call.respond(HttpStatusCode.Conflict, "Some fields are blank!");
            return@post
        }

        val lecture = Lecture(
            name = request.name,
            teacherId = ObjectId(request.teacherId),
            joinCode = (1..8).map { ('a'..'z').random() }.joinToString(""),
            isJoinable = false,
            active = true,
            quizIds = emptyList(),
            studentIds = emptyList()

            );
        println("HI-outside1!!!!!!!!!!!!!")
        val wasAcknowledged = lectureDataSource.createLecture(lecture)
        println("HI-outside2!!!!!!!!!!!!!")

        if (!wasAcknowledged) { // Error inserting new user into DB
            call.respond(HttpStatusCode.Conflict, "Unable to create lecture! Database Error.");
            return@post
        }

        call.respond(HttpStatusCode.OK, "Lecture Created!");
    }
}

fun Route.deleteLecture(lectureDataSource: LectureDataSource) {
    delete("deleteLecture") {


        val request = kotlin.runCatching { call.receiveNullable<DeleteLectureRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@delete
        }

        val lecture = lectureDataSource.deleteLecture(request.lectureId)

        call.respond(HttpStatusCode.OK, DeleteLectureResponse("Deletion was successful"))

    }
}
fun Route.getQuizFromLecture() {
    get("getQuizFromLecture") {
        call.respond(HttpStatusCode.OK)
    }
}

/*

fun Route.getLectureById(
    lectureDataSource: LectureDataSource,
    hashingService: HashingService
) {
    get("getLectureById") {
        val request = kotlin.runCatching { call.receiveNullable<GetLectureByIDRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val questionId = request.lectureId
        val selectedQuestion = lectureDataSource.getQuestion(questionId)

        if (selectedQuestion == null) {
            call.respond(HttpStatusCode.BadRequest, "selectedq was null")
            return@get
        }
        val lectureResponse =
            LectureResponse(
                question = selectedQuestion.question,
                options = selectedQuestion.options,
                responses = selectedQuestion.responses,
                answer = selectedQuestion.answer,
            )

        call.respond(
            HttpStatusCode.OK,
            message = lectureResponse
        )
    }
}

 */