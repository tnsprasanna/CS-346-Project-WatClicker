package com.backend.routes

import Quiz
import com.backend.data.Constants
import com.backend.data.questions.Question
import com.backend.data.questions.QuestionDataSource
import com.backend.data.quiz.QuizDataSource
import com.backend.data.requests.*
import com.backend.data.responses.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId

fun Route.createQuiz(quizDataSource: QuizDataSource, questionDataSource: QuestionDataSource) {
    post("createQuiz") {
        val request = kotlin.runCatching { call.receiveNullable<QuizRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

            if (request.state != Constants.HIDDEN && request.state != Constants.FINISHED
                && request.state != Constants.CLOSED && request.state != Constants.OPEN
            ) {
                call.respond(
                    HttpStatusCode.Conflict,
                    "State should be one of HIDDEN, CLOSED, FINISHED or OPEN, given state is ${request.state}"
                )
                return@post;
            }

        for (questionId in request.questions) {
            if (!ObjectId.isValid(questionId) || questionDataSource.getQuestion(questionId) == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid question selected.")
                return@post
            }
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


fun Route.getQuiz(quizDataSource: QuizDataSource) { //maybe getQuestions
    get("getQuiz") {
        val request = kotlin.runCatching { call.receiveNullable<GetQuizRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val quiz = quizDataSource.getQuiz(request.quizId)

        if (quiz != null) {
            if (quiz.state == Constants.CLOSED || quiz.state == Constants.HIDDEN) {
                call.respond(
                    HttpStatusCode.Conflict,
                    "State should be OPEN or FINISHED for questions to be visible"
                )
            }
        } else {
            call.respond(HttpStatusCode.Conflict, "Quiz is NULL")
        }

        if (quiz != null) {
            call.respond(HttpStatusCode.OK, GetQuizResponse(quiz.questions))
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

        call.respond(HttpStatusCode.OK, DeleteQuizResponse("Deletion was successful"))

    }
}


fun Route.getQuizAnswersForStudent(
    quizDataSource: QuizDataSource,
    questionDataSource: QuestionDataSource
) {
    get("getQuizAnswersForStudent") {
        val request = kotlin.runCatching { call.receiveNullable<GetQuizAnswersForStudentRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val quizId = request.quizId;
        val studentId = request.studentId;

        val quiz = quizDataSource.getQuiz(quizId);

        if (quiz == null) {
            call.respond(HttpStatusCode.BadRequest, "No such quiz exists")
            return@get
        } else if (quiz.state != "FINISHED") {
            call.respond(HttpStatusCode.Conflict, "Quiz answers are not available")
            return@get
        }

        val questions = quiz.questions;

        val questionList: MutableList<GetQuizAnswersForStudentResponse> = mutableListOf();

        for (question in questions) {
            val q = questionDataSource.getQuestion(question);
            if (q != null) {
                val qr = GetQuizAnswersForStudentResponse(q.id.toString(), q.question, q.options, q.responses, q.answer)
                questionList.add(qr);
            }
        }

        val response = GetQuizAnswersForStudentListResponse(questionList)

        call.respond(
            HttpStatusCode.OK,
            message = response
        )
    }
}
