package com.backend.routes

import Quiz
import com.backend.data.Constants
import com.backend.data.lecture.LectureDataSource
import com.backend.data.questions.QuestionDataSource
import com.backend.data.quiz.QuizDataSource
import com.backend.data.requests.*
import com.backend.data.requests.GetQuizQuestionIdsRequest
import com.backend.data.responses.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId
import java.lang.constant.ConstantDescs.NULL
import java.util.UUID

fun Route.createQuiz(quizDataSource: QuizDataSource, questionDataSource: QuestionDataSource, lectureDataSource: LectureDataSource) {
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

        // check that question id exists
        for (questionId in request.questionIds) {
            if (questionDataSource.getQuestion(questionId) == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid question selected.")
                return@post
            }
        }

            val quizId =  UUID.randomUUID().toString();
            val quiz = Quiz(
                quizId = quizId,
                name = request.name,
                state = request.state,
                questionIds = request.questionIds,
                lectureId = ObjectId(request.lectureId)
            );




            // Try to insert new user into DB
            val wasAcknowledged = quizDataSource.createQuiz(quiz);
        println("titanic")
        println(quizId)
        val classSectionAdd = lectureDataSource.addQuizToClassSection(request.lectureId, quizId)
        if (!classSectionAdd) {
            call.respond(HttpStatusCode.BadRequest, "Couldn't add quiz to class!")
            return@post
        }


            if (!wasAcknowledged) { // Error inserting new user into DB
                call.respond(HttpStatusCode.Conflict, "Unable to create quiz. Database Error.");
                return@post
            }

            call.respond(HttpStatusCode.OK, "${quizId} Quiz Created!");

        }
    }


fun Route.getQuizQuestions(quizDataSource: QuizDataSource) { //maybe getQuestions
    get("getQuizQuestions") {
        val request = kotlin.runCatching { call.receiveNullable<GetQuizQuestionIdsRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val quiz = quizDataSource.getQuizQuestions(request.quizId)

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
            call.respond(HttpStatusCode.OK, GetQuizQuestionIdsResponse(questionIds = quiz.questionIds))
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
            println()
            call.respond(HttpStatusCode.BadRequest)
            return@delete
        }

        val quiz = quizDataSource.deleteQuiz(request.quizId)
        call.respond(HttpStatusCode.OK, DeleteQuizResponse("${request.quizId} has been deleted"))

    }
}

fun Route.getQuizzes(quizDataSource: QuizDataSource) {
    get("getQuizzes") {
        val quizzes = quizDataSource.getQuizzes();
        val quizIds = mutableListOf<String>();

        if (quizzes == NULL) {
            call.respond(HttpStatusCode.Conflict, "List of quizzes returned null")
        }
        else if (quizzes.isEmpty()) {
            call.respond(HttpStatusCode.Conflict, "No quizzes available")
        }

        quizzes.forEach{
            quiz ->  quizIds.add(quiz.name)
        }

        call.respond(HttpStatusCode.OK, GetQuizzesResponse(quizIds))
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

        val quiz = quizDataSource.getQuizQuestions(quizId);

        if (quiz == null) {
            call.respond(HttpStatusCode.BadRequest, "No such quiz exists")
            return@get
        } else if (quiz.state != "FINISHED") {
            call.respond(HttpStatusCode.Conflict, "Quiz answers are not available")
            return@get
        }

        val questions = quiz.questionIds;

        val questionList: MutableList<GetQuizAnswersForStudentResponse> = mutableListOf();

        for (question in questions) {
            val q = questionDataSource.getQuestion(question);
            if (q != null) {
                val qr = GetQuizAnswersForStudentResponse(q.questionId, q.question, q.options, q.responses, q.answer)
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
