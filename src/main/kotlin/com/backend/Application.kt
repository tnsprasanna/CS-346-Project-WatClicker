package com.backend

import User
import com.`346Proj`.plugins.configureSockets
import com.backend.data.classSection.MongoClassSectionDataSource
import com.backend.data.questions.MongoQuestionDataSource
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
import com.backend.data.selection.MongoSelectionDataSource
import org.litote.kmongo.coroutine.CoroutineDatabase

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    val testing = System.getProperty("ktor.environment") == "test"

    val mongoUserName: String
    val mongoPWD: String
    val mongoDBName: String
    val db: CoroutineDatabase

    if (testing) {
        // Configuration for test database
        mongoUserName = System.getenv("MONGODB_TEST_USERNAME") ?: "backendTest"
        mongoPWD = System.getenv("MONGODB_TEST_PWD") ?: "nLvf7GtBjzAmNdUY"
        mongoDBName = System.getenv("MONGODB_TEST_NAME") ?: "db2"

        db = KMongo.createClient(
            connectionString = "mongodb+srv://$mongoUserName:$mongoPWD@cluster0.gip11qi.mongodb.net/$mongoDBName?retryWrites=true&w=majority"
        ).coroutine
            .getDatabase(mongoDBName)


    } else {
        // Configuration for regular database
        mongoUserName = System.getenv("MONGODB_USERNAME") ?: "backend"
        mongoPWD = System.getenv("MONGODB_PWD") ?: "3Vdek4PjNBEhu00O"
        mongoDBName = System.getenv("MONGODB_NAME") ?: "db1"

        db = KMongo.createClient(
            connectionString = "mongodb+srv://$mongoUserName:$mongoPWD@cluster0.3mqtfy8.mongodb.net/$mongoDBName?retryWrites=true&w=majority"
        ).coroutine
            .getDatabase(mongoDBName)
    }

    val userDataSource = MongoUserDataSource(db);
    val quizDataSource = MongoQuizDataSource(db);
    val questionDataSource = MongoQuestionDataSource(db);
    val classSectionDataSource = MongoClassSectionDataSource(db);
    val selectionDataSource = MongoSelectionDataSource(db);
    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")?: "JF8sFEEzZw"
    )
    val hashingService = SHA256HashingService()
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureSecurity(tokenConfig)
    configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService,
        tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
}
