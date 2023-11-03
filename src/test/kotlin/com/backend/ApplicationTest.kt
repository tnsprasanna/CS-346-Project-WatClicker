package com.backend

import com.backend.data.lecture.MongoLectureDataSource
import com.backend.data.questions.MongoQuestionDataSource
import com.backend.data.quiz.MongoQuizDataSource
import com.backend.data.selection.MongoSelectionDataSource
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
    val selectionDataSource = MongoSelectionDataSource(db);

    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("CS 346 Proj Backend is Running!", bodyAsText())
        }
    }



    @Test // Username Taken
    fun testSignUp1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            assertEquals(HttpStatusCode.Conflict, status)
        }
    }

    @Test // Invalid Username
    fun testSignIn2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
        }
    }

    @Test // Invalid Password
    fun testSignIn4() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            assertEquals("Unable to Sign-In! Username not found.", bodyAsText())
        }
    }

    @Test // Valid
    fun testGetQuestion1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test // Invalid
    fun testGetQuestion2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test // Invalid
    fun testCreateQuiz2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test // Invalid
    fun testCreateQuiz3() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
        }
    }

    @Test
    fun testGetQuiz1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            assertEquals(HttpStatusCode.NotFound, status)
//            assertEquals("State should be OPEN or FINISHED for questions to be visible", bodyAsText())
        }
    }

    @Test
    fun testGetQuiz2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun testGetQuiz3() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun testChangeState() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testDeleteQuiz() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
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
        }
    }



    /* TESTS FOR SPRINT 2 */
    /*
























     */


    @Test
    fun testGetUsers() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getUsers")

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }

    @Test
    fun testGetStudents() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getStudents")

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }

    @Test
    fun testGetTeachers() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getTeachers")

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }

    @Test
    fun testGetUserById() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6531fc9e13ed7a4cc0bf3bc0"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getUserById")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }

    @Test
    fun testIsStudentFromId() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6531fc9e13ed7a4cc0bf3bc0"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getStudentFromId")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun testIsTeacherFromId() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6531fc9e13ed7a4cc0bf3bc0"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getTeacherFromId")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun testIsStudentFromUsername() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "username": "dawson12@uwaterloo.ca"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/isStudentFromUsername")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }

    @Test
    fun testIsTeacherFromUsername() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "username": "dawson12@uwaterloo.ca"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/isTeacherFromUsername")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }


    @Test
    fun testChangeRole() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6531fc9e13ed7a4cc0bf3bc0"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeRole")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }


    @Test
    fun testChangeFirstName() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6531fc9e13ed7a4cc0bf3bc0",
            "newFirstName": "nfn",
            "newLastName": ""
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeFirstName")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }

    @Test
    fun testChangeLastName() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6531fc9e13ed7a4cc0bf3bc0",
            "newFirstName" : "",
            "newLastName": "nln"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeLastName")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }

    @Test
    fun testChangeFirstAndLastName() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6531fc9e13ed7a4cc0bf3bc0",
            "newFirstName" : "nfn",
            "newLastName": "nln"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeFirstAndLastName")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }

    @Test
    fun testChangeUsername() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6531fc9e13ed7a4cc0bf3bc0",
            "newUsername": "nun
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeUsername")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }


    @Test
    fun testGetClassSections() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6531fc9e13ed7a4cc0bf3bc0",
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getClassSections")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }

    @Test
    fun testGetClassSectionJoinableStatus() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "classSectionId": "6531fc9e13ed7a4cc0bf3bc0",
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getClassSectionJoinableStatus")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }

    @Test
    fun testGetClassSectionJoinCode() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "classSectionId": "6531fc9e13ed7a4cc0bf3bc0",
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getClassSectionJoinCode")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }

    @Test
    fun testMakeClassSectionJoinable() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "classSectionId": "6531fc9e13ed7a4cc0bf3bc0",
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/makeClassSectionJoinable")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }

    @Test
    fun testMakeClassSectionUnjoinable() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "classSectionId": "6531fc9e13ed7a4cc0bf3bc0",
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/makeClassSectionUnjoinable")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }

    @Test
    fun testJoinClassSection() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, lectureDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6531fc9e13ed7a4cc0bf3bc0",
            "classSectionId": "6531fc9e13ed7a4cc0cf3bc0"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/joinClassSection")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.MethodNotAllowed, status)
        }
    }














}
