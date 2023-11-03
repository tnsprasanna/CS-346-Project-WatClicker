package com.backend

import com.backend.data.lecture.MongoLectureDataSource
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
    val lectureDataSource = MongoLectureDataSource(db);

    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("CS 346 Proj Backend is Running!", bodyAsText())
        }
    }



    @Test // Username Taken
    fun testSignUp1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
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

    @Test // Valid
    fun testGetQuestion1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
        }

        val requestBody = """
        {
            "questionId": "653204f2b4133a26845ac5ec"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuestion")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test // Invalid
    fun testGetQuestion2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
        }

        val requestBody = """
        {
            "questionId": "65320484d913da7802f4a427"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuestion")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            assertEquals("selectedq was null", bodyAsText())
        }
    }

    @Test // Invalid
    fun testAddQuestion1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
        }

        val requestBody = """
        {
            "question" : "testQ12",
            "options": ["op1", "op2"],
            "responses": [0, 0],
            "answer": 0
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/addQuestion")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test // Valid
    fun testDeleteQuestion2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
        }

        val requestBody = """
        {
            "questionId": "65320484d913da7802f4a427"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuestion")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
//            assertEquals("deletion successful", bodyAsText())
        }
    }

    @Test // Valid
    fun testCreateQuiz1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
        }

        val requestBody = """
        {
            "name": "testq1",
            "state": "HIDDEN",
            "questions": ["653204f2b4133a26845ac5ec", "6532050b0095e01223740fff"]
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/createQuiz")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Quiz Created!", bodyAsText())
        }
    }

    @Test // Invalid
    fun testCreateQuiz2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
        }

        val requestBody = """
        {
            "name": "testq2",
            "state": "bruh",
            "questions": ["653204f2b4133a26845ac5ec", "6532050b0095e01223740fff"]
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/createQuiz")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("State should be one of HIDDEN, CLOSED, FINISHED or OPEN, given state is bruh", bodyAsText())
        }
    }

    @Test // Invalid
    fun testCreateQuiz3() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
        }

        val requestBody = """
        {
            "name": "testq3",
            "state": "OPEN",
            "questions": ["", "6532050b0095e01223740fff"]
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/createQuiz")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            assertEquals("Invalid question selected.", bodyAsText())
        }
    }

    @Test
    fun testGetQuiz1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
        }

        val requestBody = """
        {
            "quizId": "6531f3843d115778d99ebc73"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuiz")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("State should be OPEN or FINISHED for questions to be visible", bodyAsText())
        }
    }

    @Test
    fun testGetQuiz2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
        }

        val requestBody = """
        {
            "quizId": "6531dc554aa6dd5caca90987"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuiz")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("Quiz is NULL", bodyAsText())
        }
    }

    @Test
    fun testGetQuiz3() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
        }

        val requestBody = """
        {
            "quizId": "6531f3cfe9f50d572b6b1ed5"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuiz")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testChangeState() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
        }

        val requestBody = """
        {
            "quizId": "6531fe5d68b6f475b3959362",
            "newState: "CLOSED"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Patch
        requestBuilder.url("/changeState")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.patch(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testDeleteQuiz() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource)
        }

        val requestBody = """
        {
            "quizId": "6531fc9e13ed7a4cc0bf3bc0"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Delete
        requestBuilder.url("/deleteQuiz")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("{\"deletedQuiz\":\"Deletion was successful\"}", bodyAsText())
        }
    }

}
