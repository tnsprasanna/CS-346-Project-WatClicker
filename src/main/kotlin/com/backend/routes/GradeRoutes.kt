package com.backend.routes

import com.backend.data.classSection.ClassSectionDataSource
import com.backend.data.questions.QuestionDataSource
import com.backend.data.quiz.QuizDataSource
import com.backend.data.requests.ClassSectionIdRequest
import com.backend.data.requests.QuizIdRequest
import com.backend.data.user.UserDataSource
import com.backend.routes.authenticate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.teacherGetGradesForClass(
    userDataSource: UserDataSource,
    classSectionDataSource: ClassSectionDataSource,
    quizDataSource: QuizDataSource,
    questionDataSource: QuestionDataSource
) {
    authenticate{
        get("teacherGetGradesForClass") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@get
            }

            val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@get
            }

            val user = userDataSource.getUserById(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@get
            }

            val classSection = classSectionDataSource.getClassSectionById(request.classSectionId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@get
            }

            if (classSection.teacherId.toString() != userId) {
                call.respond(HttpStatusCode.Conflict, "Caller is not the teacher for this class!")
                return@get
            }

            println("REACHED")
            println("GOING TO GET ALL GRADES")

            val gradesCSV = classSectionDataSource.gradesForAllStudents(request.classSectionId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "Error getting grades")
                return@get
            }

            call.respond(HttpStatusCode.OK, message = gradesCSV )
            return@get
        }
    }
}

fun Route.teacherGetGradesForQuiz(
    userDataSource: UserDataSource,
    classSectionDataSource: ClassSectionDataSource,
    quizDataSource: QuizDataSource,
    questionDataSource: QuestionDataSource
) {
    authenticate{
        get("teacherGetGradesForQuiz") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@get
            }

            val request = kotlin.runCatching { call.receiveNullable<QuizIdRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@get
            }

            val user = userDataSource.getUserById(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@get
            }

            val quiz = quizDataSource.getQuizById(request.quizId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "Quiz not found!")
                return@get
            }

            val classSection = classSectionDataSource.getClassSectionById(quiz.classSectionId.toString())?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@get
            }

            if (classSection.teacherId.toString() != userId) {
                call.respond(HttpStatusCode.Conflict, "Caller is not the teacher for this class!")
                return@get
            }

            val gradesCSV = quizDataSource.gradesForAllStudents(request.quizId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "Error getting grades")
                return@get
            }

            call.respond(HttpStatusCode.OK, message = gradesCSV )
            return@get

        }
    }
}



// Student
fun Route.studentGetGradesForClass(
    userDataSource: UserDataSource,
    classSectionDataSource: ClassSectionDataSource,
    quizDataSource: QuizDataSource,
    questionDataSource: QuestionDataSource
) {
    authenticate{
        get("studentGetGradesForClass") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@get
            }

            val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@get
            }

            val user = userDataSource.getUserById(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@get
            }

            val classSection = classSectionDataSource.getClassSectionById(request.classSectionId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@get
            }

            if (!classSection.studentIds.contains(user.id)) {
                call.respond(HttpStatusCode.Conflict, "Caller is not a student of this class!")
                return@get
            }

            val gradesCSV = classSectionDataSource.gradesForStudent(request.classSectionId, userId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "Error getting grades")
                return@get
            }

            call.respond(HttpStatusCode.OK, message = gradesCSV )
            return@get
        }
    }
}

fun Route.studentsGetGradesForQuiz(
    userDataSource: UserDataSource,
    classSectionDataSource: ClassSectionDataSource,
    quizDataSource: QuizDataSource,
    questionDataSource: QuestionDataSource
) {
    authenticate{
        get("studentsGetGradesForQuiz") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@get
            }

            val request = kotlin.runCatching { call.receiveNullable<QuizIdRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@get
            }

            val user = userDataSource.getUserById(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@get
            }

            val quiz = quizDataSource.getQuizById(request.quizId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "Quiz not found!")
                return@get
            }

            val classSection = classSectionDataSource.getClassSectionById(quiz.classSectionId.toString())?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@get
            }

            if (!classSection.studentIds.contains(user.id)) {
                call.respond(HttpStatusCode.Conflict, "Caller is not a student of this class!")
                return@get
            }

            val gradesCSV = quizDataSource.gradesForStudent(request.quizId, studentId = userId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "Error getting grades")
                return@get
            }

            call.respond(HttpStatusCode.OK, message = gradesCSV )
            return@get

        }
    }
}