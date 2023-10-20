package com.backend.plugins

import com.backend.authenticate
import com.backend.*
import com.backend.data.quiz.QuizDataSource
import com.backend.data.user.UserDataSource
import com.backend.getSecretInfo
import com.backend.routes.*
import com.backend.data.questions.QuestionDataSource
import com.backend.routes.getSecretInfo
import com.backend.security.hashing.HashingService
import com.backend.security.token.TokenConfig
import com.backend.security.token.TokenService
import com.backend.signIn
import com.backend.signUp
import com.backend.routes.signIn
import com.backend.routes.signUp
import com.backend.routes.addQuestion
import com.backend.routes.getQuestion
import com.backend.routes.deleteQuestion
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    quizDataSource: QuizDataSource,
    questionDataSource: QuestionDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        signUp(userDataSource, hashingService)
        authenticate()
        getSecretInfo()
        addQuestion(questionDataSource, hashingService)
        getQuestion(questionDataSource, hashingService)
        deleteQuestion(questionDataSource, hashingService)
        getClassSections()
        getUsers(userDataSource)
        getStudents(userDataSource)
        getTeachers(userDataSource)
        createQuiz(quizDataSource)
        getQuiz(quizDataSource)
        changeState(quizDataSource)
        deleteQuiz(quizDataSource)

        get("") {
            call.respond(HttpStatusCode.OK, "CS 346 Proj Backend is Running!")
        }
    }
}
