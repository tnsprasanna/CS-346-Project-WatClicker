package com.backend

import User
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
import com.backend.data.lecture.MongoLectureDataSource
import com.backend.data.selection.MongoSelectionDataSource

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val mongoUserName: String = System.getenv("MONGODB_USERNAME")?: "backend"
    val mongoPWD = System.getenv("MONGODB_PWD")?: "3Vdek4PjNBEhu00O"
    val mongoDBName = System.getenv("MONGODB_NAME")?: "db1"

    val db = KMongo.createClient(
        connectionString = "mongodb+srv://$mongoUserName:$mongoPWD@cluster0.3mqtfy8.mongodb.net/$mongoDBName?retryWrites=true&w=majority"
    ).coroutine
        .getDatabase(mongoDBName)

    val userDataSource = MongoUserDataSource(db);
    val quizDataSource = MongoQuizDataSource(db);
    val questionDataSource = MongoQuestionDataSource(db);
    val lectureDataSource = MongoLectureDataSource(db);
    val selectionDataSource = MongoSelectionDataSource(db);
    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")?: "JF8sFEEzZw"
    )
    val hashingService = SHA256HashingService()


    configureSerialization()
    configureMonitoring()
    configureSecurity(tokenConfig)
    configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService,
        tokenService, tokenConfig, lectureDataSource, selectionDataSource)
}

/*
- signup - DONE
- login - DONE
- are they a teacher/student - DONE
- getQuestions for a quiz - DONE


- teacher can createQuiz - VERIFY IT'S A TEACHER - VIPASHA

- teacher can create new class  - SINEHA
- get classes for student and teacher - DONE BUT NEEDS TESTING

- getJoinableStatus
- teacher can make class joinable or unjoinable
- teacher can get join code

- allow students to join class using a join code





- get quizlist for a class

- student submit their quiz answers
- change quiz states DONE
- get quiz state
*/