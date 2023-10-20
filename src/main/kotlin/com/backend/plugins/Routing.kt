package com.backend.plugins

import com.backend.*
import com.backend.data.quiz.QuizDataSource
import com.backend.data.user.UserDataSource
import com.backend.security.hashing.HashingService
import com.backend.security.token.TokenConfig
import com.backend.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    quizDataSource: QuizDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        signUp(userDataSource, hashingService)
        authenticate()
        getSecretInfo()
        createQuiz(quizDataSource)

        get("") {
            call.respond(HttpStatusCode.OK, "CS 346 Proj Backend is Running!")
        }
    }
}
