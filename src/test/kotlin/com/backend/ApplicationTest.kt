package com.backend

import com.backend.data.questions.MongoQuestionDataSource
import com.backend.data.quiz.MongoQuizDataSource
import com.backend.data.user.MongoUserDataSource
import com.backend.plugins.*
import com.backend.security.hashing.SHA256HashingService
import com.backend.security.token.JwtTokenService
import com.backend.security.token.TokenConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.testing.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import kotlin.test.*

class ApplicationTest {
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
    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = "issuer",
        audience = "audience",
        expiresIn = 365L * 1000L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")?: "JF8sFEEzZw"
    )
    val hashingService = SHA256HashingService()
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig)
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("CS 346 Proj Backend is Running!", bodyAsText())
        }
    }



    @Test // Username Taken
    fun testSignUp1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig)
        }

        val requestBody = """
        {
            "username": "Drake4@uwaterloo.ca",
            "password": "my-password",
            "role": "STUDENT",
            "firstname": "Drake",
            "lastname": "Rapper"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/signup")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("Username Taken! Please use another username.", bodyAsText())
        }
    }

    @Test // Password Length too Small
    fun testSignUp2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig)
        }

        val requestBody = """
        {
            "username": "Drake4@uwaterloo.ca",
            "password": "my",
            "role": "STUDENT",
            "firstname": "Drake",
            "lastname": "Rapper"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/signup")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("Password is too short! Length should be >= 8.", bodyAsText())
        }
    }

    @Test // Invalid Role
    fun testSignUp3() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig)
        }

        val requestBody = """
        {
            "username": "Eminem@uwaterloo.ca",
            "password": "my-password",
            "role": "RAP-GOD",
            "firstname": "Marshall",
            "lastname": "Rapper"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/signup")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("Role is invalid! Should be 'TEACHER' or 'STUDENT'.", bodyAsText())
        }
    }

    @Test // Invalid Role
    fun testSignUp4() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig)
        }

        val requestBody = """
        {
            "username": "Eminem@uwaterloo.ca",
            "password": "my-password",
            "role": "RAP-GOD",
            "firstname": "Marshall",
            "lastname": "Rapper"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/signup")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("Role is invalid! Should be 'TEACHER' or 'STUDENT'.", bodyAsText())
        }
    }

    @Test // Empty Field(s)
    fun testSignUp5() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig)
        }

        val requestBody = """
        {
            "username": "Eminem@uwaterloo.ca",
            "password": "my-password",
            "role": "STUDENT",
            "firstname": "Marshall",
            "lastname": ""
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/signup")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("Some fields are blank!", bodyAsText())
        }
    }

    @Test // Valid
    fun testSignIn1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig)
        }

        val requestBody = """
        {
            "username": "Drake3@uwaterloo.ca",
            "password": "my-password"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/signin")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test // Invalid Username
    fun testSignIn2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig)
        }

        val requestBody = """
        {
            "username": "obama@uwaterloo.ca",
            "password": "my-password"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/signin")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("Unable to Sign-In! Username not found.", bodyAsText())
        }
    }

    @Test // Invalid Password
    fun testSignIn3() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig)
        }

        val requestBody = """
        {
            "username": "Drake3@uwaterloo.ca",
            "password": "my-passwordd"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/signin")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("Unable to Sign-In! Password Incorrect.", bodyAsText())
        }
    }

    @Test // Invalid Password
    fun testSignIn4() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig)
        }

        val requestBody = """
        {
            "username": "Drake3@uwaterloo.ca",
            "password": "my-passwordd"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/signin")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("Unable to Sign-In! Password Incorrect.", bodyAsText())
        }
    }
}
