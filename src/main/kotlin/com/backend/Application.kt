package com.backend

import User
import com.backend.data.quiz.MongoQuizDataSource
import com.backend.data.user.MongoUserDataSource
import com.backend.plugins.*
import com.backend.security.hashing.SHA256HashingService
import com.backend.security.token.JwtTokenService
import com.backend.security.token.TokenConfig
import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val mongoUserName: String = System.getenv("MONGODB_USERNAME")
    val mongoPWD = System.getenv("MONGODB_PWD")
    val mongoDBName = System.getenv("MONGODB_NAME")

    val db = KMongo.createClient(
        connectionString = "mongodb+srv://$mongoUserName:$mongoPWD@cluster0.3mqtfy8.mongodb.net/$mongoDBName?retryWrites=true&w=majority"
    ).coroutine
        .getDatabase(mongoDBName)

    val userDataSource = MongoUserDataSource(db);
    val questionDataSource = MongoQuestionDataSource(db)
    val quizDataSource = MongoQuizDataSource(db);
    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")
    )
    val hashingService = SHA256HashingService()

    configureSerialization()
    configureMonitoring()
    configureSecurity(tokenConfig)
    configureRouting(userDataSource, quizDataSource, hashingService, tokenService, tokenConfig)
    configureRouting(userDataSource, questionDataSource, hashingService, tokenService, tokenConfig)
}

