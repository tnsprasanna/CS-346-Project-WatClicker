package com.backend.plugins

import com.backend.*
import com.backend.data.quiz.QuizDataSource
import com.backend.routes.authenticate
import com.backend.data.user.UserDataSource
import com.backend.routes.*
import com.backend.data.questions.QuestionDataSource
import com.backend.routes.getSecretInfo
import com.backend.security.hashing.HashingService
import com.backend.security.token.TokenConfig
import com.backend.security.token.TokenService
import com.backend.routes.signIn
import com.backend.routes.signUp
import com.backend.routes.addQuestion
import com.backend.routes.getQuestion
import com.backend.routes.deleteQuestion
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.backend.data.lecture.LectureDataSource


fun Application.configureRouting(
    userDataSource: UserDataSource,
    questionDataSource: QuestionDataSource,
    quizDataSource: QuizDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    lectureDataSource: LectureDataSource
) {
    routing {
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        signUp(userDataSource, hashingService)
        authenticate()
        getSecretInfo(userDataSource)
        getClassSections()
        getUsers(userDataSource)
        getStudents(userDataSource)
        getTeachers(userDataSource)
        addQuestion(questionDataSource, hashingService)
        createQuiz(quizDataSource, questionDataSource)
        getQuestion(questionDataSource, hashingService)
        deleteQuestion(questionDataSource, hashingService)
        getQuizQuestions(quizDataSource)
        changeState(quizDataSource)
        deleteQuiz(quizDataSource)
        getUserById(userDataSource)
        isStudentFromId(userDataSource)
        isStudentFromUsername(userDataSource)
        isTeacherFromId(userDataSource)
        isTeacherFromUsername(userDataSource)
        deleteUser(userDataSource)
        changeRole(userDataSource)
        changeFirstName(userDataSource)
        changeLastName(userDataSource)
        changeFirstAndLastName(userDataSource)
        changeUsername(userDataSource)
        getQuizzes(quizDataSource)
        createLecture(lectureDataSource)
        deleteLecture(lectureDataSource)

        get("") {
            call.respond(HttpStatusCode.OK, "CS 346 Proj Backend is Running!")
        }
    }
}
