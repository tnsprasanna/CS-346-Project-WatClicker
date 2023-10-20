package com.backend.plugins

import com.backend.data.user.UserDataSource
import com.backend.routes.*
import com.backend.security.hashing.HashingService
import com.backend.security.token.TokenConfig
import com.backend.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        signUp(userDataSource, hashingService)
        authenticate()
        getSecretInfo()
        getClassSections()
        getUsers(userDataSource)
        getStudents(userDataSource)
        getTeachers(userDataSource)

        get("") {
            call.respond(HttpStatusCode.OK, "CS 346 Proj Backend is Running!")
        }
    }
}
