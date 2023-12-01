package com.backend

import com.backend.data.classSection.MongoClassSectionDataSource
import com.backend.data.questions.MongoQuestionDataSource
import com.backend.data.quiz.MongoQuizDataSource
import com.backend.data.responses.ClassSectionResponse
import com.backend.data.selection.MongoSelectionDataSource
import com.backend.data.user.MongoUserDataSource
import com.backend.plugins.*
import com.backend.security.hashing.SHA256HashingService
import com.backend.security.token.JwtTokenService
import com.backend.security.token.TokenConfig
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.testing.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import kotlin.test.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.util.UUID
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ApplicationTest {
    val mongoUserName = System.getenv("MONGODB_TEST_USERNAME") ?: "backendTest"
    val mongoPWD = System.getenv("MONGODB_TEST_PWD") ?: "nLvf7GtBjzAmNdUY"
    val mongoDBName = System.getenv("MONGODB_TEST_NAME") ?: "db2"

    val db = KMongo.createClient(
        connectionString = "mongodb+srv://$mongoUserName:$mongoPWD@cluster0.gip11qi.mongodb.net/$mongoDBName?retryWrites=true&w=majority"
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
    val classSectionDataSource = MongoClassSectionDataSource(db);
    val selectionDataSource = MongoSelectionDataSource(db);

    // COMMON OBJECT IDS
    val classSectionIdVar = "65604654538196181f792f61"
    val fixedquizid = "6557cf5e538196181f792f51"
    val userId = "6557cdd8131c9e2118339a51"
    val fixedquestionid = "6557d7efca5e000bac363b27"
    val deletedclassSectionIdVar = "6562aeec75779d61c73c812b"


    suspend fun obtainJwtToken(client: HttpClient): String {
        val signInRequestBody = """
    {
        "username": "teacher1@uwaterloo.ca",
        "password": "password"
    }
    """

        val response = client.post("/signin") {
            setBody(TextContent(signInRequestBody, ContentType.Application.Json))
        }

        if (response.status == HttpStatusCode.OK) {
            print(response.bodyAsText())
            @Serializable
            data class TokenResponse(val token: String)

            val jsonString = response.bodyAsText()
            val json = Json { ignoreUnknownKeys = true } // Create a Json instance with configuration
            val tokenResponse = json.decodeFromString<TokenResponse>(jsonString)
            println("The token is: ${tokenResponse.token}")
            return tokenResponse.token
        } else {
            throw IllegalStateException("Failed to obtain JWT token, status: ${response.status}")
        }
    }

    suspend fun createClassSectionAndGetId(client: HttpClient): String {
        println("Function start")
        val createRequestBody = """
    {
        "name": "ExampleName2",
        "isJoinable": true
    }
    """
        println("Request body prepared")

        try {
            val jwtToken = obtainJwtToken(client)
            println("JWT Token obtained: $jwtToken")

            val response = client.post("/createClassSection") {
                header("Authorization", "Bearer $jwtToken")
                setBody(TextContent(createRequestBody, ContentType.Application.Json))
            }

            println("Response received with status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                @Serializable
                data class ClassSectionResponse(val id: String)

                val jsonString = response.bodyAsText()
                println("Response JSON: $jsonString")

                val json = Json { ignoreUnknownKeys = true }
                val classSectionResponse = json.decodeFromString<ClassSectionResponse>(jsonString)
                println("Created Class Section with ID: ${classSectionResponse.id}")
                return classSectionResponse.id
            } else {
                throw IllegalStateException("Failed to create class section, status: ${response.status}")
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
            throw e
        }
    }



    suspend fun obtainJwtTokenStudent(client: HttpClient): String {
        val signInRequestBody = """
    {
        "username": "sineha@uwaterloo.ca",
        "password": "password"
    }
    """

        val response = client.post("/signin") {
            setBody(TextContent(signInRequestBody, ContentType.Application.Json))
        }

        if (response.status == HttpStatusCode.OK) {
            print(response.bodyAsText())
            return response.bodyAsText()
        } else {
            throw IllegalStateException("Failed to obtain JWT token, status: ${response.status}")
        }
    }


    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("CS 346 Proj Backend is Running!", bodyAsText())
        }
    }



    @Test // Username Taken
    fun testSignUp1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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

    @Test
    fun testSignUp10() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val randomPart = UUID.randomUUID().toString()
        val username = "$randomPart@uwaterloo.ca"

        val requestBody = """
    {
        "username": "$username",
        "password": "password",
        "role": "STUDENT",
        "firstname": "Harry",
        "lastname": "Potter"
    }
    """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder()

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/signup")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test // Password Length too Small
    fun testSignUp2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "questionId": "6557d7efca5e000bac363b27"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuestionById")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test // Invalid
    fun testGetQuestion2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "questionId": "inexistent"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuestionById")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            assertEquals("Question not found!", bodyAsText())
        }
    }

    @Test // Invalid
    fun testAddQuestion1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val jwtToken = obtainJwtToken(client)

        val requestBody = """
        {
            "quizId": "6557cf5e538196181f792f51",
            "question": "What is the capital of France?",
            "options": ["Paris", "London", "Berlin", "Madrid"],
            "answer": 0
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/createQuestion")
        requestBuilder.header("Authorization", "Bearer ${jwtToken}")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test // Invalid
    fun testAddQuestion2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "quizId": "6557cf5e538196181f792f51",
        "question": "What is the capital of France?",
        "options": ["Paris", "Berlin", "London", "Madrid"],
        "answer": 0
        }   
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/createQuestion")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }


    @Test // Valid
    fun testGetQuestionByID2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "questionId": "$fixedquestionid"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuestionById")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test // Valid
    fun testCreateQuiz1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val jwtToken = obtainJwtToken(client)

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
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test // Invalid
    fun testCreateQuiz2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test // Invalid
    fun testCreateQuiz3() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testGetQuiz1() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "quizId": "$fixedquizid"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuiz")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.NotFound, status)
//            assertEquals("State should be OPEN or FINISHED for questions to be visible", bodyAsText())
        }
    }

    @Test
    fun testGetQuiz2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "quizId": "dne"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuiz")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun testGetQuiz3() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "quizId": "6531f3cfe9f50d572b6b1ed5"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuiz")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun testGetQuiz4() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "quizId": "dne"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuiz")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun testDeleteQuiz() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "quizId": "6531fc9e13ed7a4cc0bf3bc0"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Delete
        requestBuilder.url("/deleteQuiz")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
        }
    }


    @Test
    fun testGetUsers() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getUsers")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetStudents() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getStudents")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetTeachers() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getTeachers")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetUserById() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6557cd000588aa3461e28016"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getUserById")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testIsStudentFromId() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6557cd000588aa3461e28016"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/isStudentFromId")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testIsTeacherFromId() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6557cd000588aa3461e28016"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/isTeacherFromId")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testIsStudentFromUsername() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "username": "teacher1@uwaterloo.ca"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/isStudentFromUsername")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testIsTeacherFromUsername() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "username": "teacher1@uwaterloo.ca"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/isTeacherFromUsername")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }


    @Test
    fun testChangeRole() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = ""

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeRole")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }


    @Test
    fun testChangeFirstName() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testChangeFirstName2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "newFirstName": "katryna",
            "newLastName": "bob"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeFirstName")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testChangeLastName() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "newFirstName" : "test",
            "newLastName": "nln"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeLastName")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testChangeFirstAndLastName() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "newFirstName" : "nfn",
            "newLastName": "nln"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeFirstAndLastName")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testChangeUsername() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }


    @Test
    fun testGetClassSections() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "userId": "6531fc9e13ed7a4cc0bf3bc0",
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getClassSections")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetClassSectionJoinableStatus() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "classSectionId": "6557ce16131c9e2118339a54"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getClassSectionJoinableStatus")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetClassSectionJoinCode() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "classSectionId": "6557ce16131c9e2118339a54"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getClassSectionJoinCode")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testMakeClassSectionJoinable() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "classSectionId": "6557ce16131c9e2118339a54"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/makeClassSectionJoinable")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testMakeClassSectionUnjoinable() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "classSectionId": "6557ce16131c9e2118339a54"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/makeClassSectionUnjoinable")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testJoinClassSection() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
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
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    // FINISH TESTING BACKEND

    @Test
    fun testGetQuestionById() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
            "questionId": "6557d7efca5e000bac363b27"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuestionById")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testCreateQuestion() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "quizId": "$fixedquizid",
        "question": "fave colour?",
        "options": ["blue", "red", "green"],
        "answer": 2
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/createQuestion")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetResponsesFromQuestion() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
          
        {
            "questionId": "6557d7efca5e000bac363b27"
        }   
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getResponsesFromQuestion")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetQuizById() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
          
        {
            "quizId": "$fixedquizid"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuizById")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testChangeQuizState() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
          
        {
            "quizId": "$fixedquizid",
            "newState": "OPEN"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeQuizState")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")

        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetQuizQuestions() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
          
        {
             "quizId": "$fixedquizid"
        }
        """



        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuizQuestions")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetResponsesForQuestionsInQuiz() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
          
        {
             "quizId": "$fixedquizid"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getResponsesForQuestionsInQuiz")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetClassSectionById() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "6557ce16131c9e2118339a54"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getClassSectionById")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetClassSectionByIdInvalid() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
          
        {
           "classSectionId": "dne"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getClassSectionById")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            assertEquals("ClassSection not found!", bodyAsText())
        }
    }

    @Test
    fun testGetClassSection() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
          
        {
           "classSectionId": "$classSectionIdVar"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getClassSectionById")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testCreateClassSection() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
          
        {
            "name": "ExampleName",
            "isJoinable": true
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/createClassSection")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testDeleteClassSection() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "$deletedclassSectionIdVar"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Delete
        requestBuilder.url("/deleteClassSection")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertTrue(status == HttpStatusCode.OK || status == HttpStatusCode.Conflict)

            when (status) {
                HttpStatusCode.OK -> assertEquals("ClassSection Deleted!", bodyAsText())
                HttpStatusCode.Conflict -> assertEquals("ClassSection not found!", bodyAsText())
                else -> fail("Unexpected status code")
            }
        }
    }
    /*
        @Test
        fun testCreateAndDeleteClassSection() = testApplication {
            application {
                configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
            }

            println("chicken")
            val classSectionIdVar = createClassSectionAndGetId(client)

            println("chicken2")

            // Step 2: Delete the created class section
            val deleteRequestBody = """
        {
            "classSectionId": "$classSectionIdVar"
        }
        """
            val jwtToken = obtainJwtToken(client)
            val deleteRequestBuilder: HttpRequestBuilder = HttpRequestBuilder()

            deleteRequestBuilder.method = HttpMethod.Delete
            deleteRequestBuilder.url("/deleteClassSection")
            deleteRequestBuilder.header("Authorization", "Bearer $jwtToken")
            deleteRequestBuilder.setBody(TextContent(deleteRequestBody, ContentType.Application.Json))

            client.delete(builder = deleteRequestBuilder).apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals("ClassSection Deleted!", bodyAsText())
            }
        }
    */

    @Test
    fun testDeleteClassSection2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "dne"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Delete
        requestBuilder.url("/deleteClassSection")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("ClassSection not found!", bodyAsText())
        }
    }

    @Test
    fun testDeleteClassSection3() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "$classSectionIdVar"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Delete
        requestBuilder.url("/deleteClassSection")
        val jwtToken = obtainJwtTokenStudent(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testDeleteClassSection4() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "$classSectionIdVar"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Delete
        requestBuilder.url("/deleteClassSection")
        val jwtToken = obtainJwtTokenStudent(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)

        }
    }

    @Test
    fun testDeleteClassSection5() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "dne"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Delete
        requestBuilder.url("/deleteClassSection")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.delete(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("ClassSection not found!", bodyAsText())
        }
    }

    @Test
    fun testGetQuizzesInClassSection() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "$classSectionIdVar"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuizzesInClassSection")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetQuizzesInClassSectionInvalid() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "123"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getQuizzesInClassSection")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("ClassSection not found!", bodyAsText())
        }
    }

    @Test
    fun testGetStudentsInClassSection() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "$classSectionIdVar"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getStudentsInClassSection")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetStudentsInClassSectionInvalid() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "dne"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getStudentsInClassSection")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("ClassSection not found!", bodyAsText())
        }
    }

    /*
    @Test
    fun testRemoveStudentFromClassSection() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "$classSectionIdVar",
        "userid": "6557cd000588aa3461e28016"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/removeStudentFromClassSection")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Student has been removed from ClassSection!", bodyAsText())
        }
    }
    */
    @Test
    fun testRemoveStudentFromClassSection2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "$classSectionIdVar",
        "userid": "6557cd000588aa3461e28016"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/removeStudentFromClassSection")
        val jwtToken = obtainJwtTokenStudent(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testRemoveStudentFromClassSection3() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "doesnotexist",
        "userid": "6557cd000588aa3461e28016"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/removeStudentFromClassSection")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("ClassSection not found!", bodyAsText())
        }
    }

    @Test
    fun testRemoveStudentFromClassSection4() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "$classSectionIdVar",
        "userid": "6557cd000588aa3461e28016"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/removeStudentFromClassSection")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testRemoveStudentFromClassSection5() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "test",
        "userid": "test"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/removeStudentFromClassSection")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("ClassSection not found!", bodyAsText())
        }
    }

    @Test
    fun testChangeClassSectionName() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        fun randomName(length: Int): String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }

        val newName = randomName(10)

        val requestBody = """
        {
        "classSectionId": "65604c99538196181f792f63",
        "newName": "$newName"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeClassSectionName")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testChangeClassSectionName2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        fun randomName(length: Int): String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }

        val newName = randomName(10)

        val requestBody = """
        {
        "classSectionId": "65604c99538196181f792f63",
        "newName": "$newName"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeClassSectionName")
        val jwtToken = obtainJwtTokenStudent(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testChangeClassSectionName3() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        fun randomName(length: Int): String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }

        val newName = randomName(10)

        val requestBody = """
        {
        "classSectionId": "dne",
        "newName": "$newName"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeClassSectionName")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("ClassSection not found!", bodyAsText())
        }
    }

    @Test
    fun testChangeClassSectionName4() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        fun randomName(length: Int): String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }

        val newName = randomName(10)

        val requestBody = """
        {
        "classSectionId": "65604c99538196181f792f63",
        "newName": "$newName"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeClassSectionName")
        val jwtToken = obtainJwtTokenStudent(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testChangeClassSectionName7() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "$classSectionIdVar"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeClassSectionName")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }


    @Test
    fun testMakeClassSectionActive() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "$classSectionIdVar"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/makeClassSectionActive")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("ClassSection is now Active!", bodyAsText())
        }
    }

    @Test
    fun testMakeClassSectionActive0() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "dne"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/makeClassSectionActive")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("ClassSection not found!", bodyAsText())
        }
    }

    @Test
    fun testMakeClassSectionActive4() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "65604ce3538196181f792f64"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/makeClassSectionActive")
        val jwtToken = obtainJwtTokenStudent(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }


    @Test
    fun testMakeClassSectionInactive() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "6557ce16131c9e2118339a54"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/makeClassSectionInactive")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("ClassSection is now inactive!", bodyAsText())
        }
    }

    @Test
    fun testMakeClassSectionInactive2() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "notfound"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/makeClassSectionInactive")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("ClassSection not found!", bodyAsText())
        }
    }

    @Test
    fun testMakeClassSectionInactive3() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "notfound"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/makeClassSectionInactive")
        val jwtToken = obtainJwtTokenStudent(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }


    @Test
    fun testMakeClassSectionInactive6() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "classSectionId": "12345"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/makeClassSectionInactive")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
        }
    }
// SELECTION

    @Test
    fun testGetSelectionById() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "selectionId": "6557d094ca5e000bac363b23"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getSelectionById")
        val jwtToken = obtainJwtTokenStudent(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetSelectionByUserAndQuestionId() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "questionId": "65604c99538196181f792f63"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Get
        requestBuilder.url("/getSelectionByUserAndQuestionId")
        val jwtToken = obtainJwtTokenStudent(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.get(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testCreateSelection() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "questionId": "65604c99538196181f792f63",
        "selectedOption": 1
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/createSelection")
        val jwtToken = obtainJwtTokenStudent(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testDeleteSelection() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "selectionId": "dne"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/deleteSelection")
        val jwtToken = obtainJwtTokenStudent(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testEditSelection() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "selectionId": "6557d094ca5e000bac363b23",
        "newOption": 0
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/editSelection")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.Conflict, status)
            assertEquals("User must be a student!", bodyAsText())
        }
    }

    @Test
    fun testChangeQuizName() = testApplication {
        application {
            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
        }

        val requestBody = """
        {
        "quizId": "$fixedquizid",
        "newName": "QUIZ OF THE WEEK"
        }
        """

        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();

        requestBuilder.method = HttpMethod.Post
        requestBuilder.url("/changeQuizName")
        val jwtToken = obtainJwtToken(client)
        requestBuilder.header("Authorization", "Bearer $jwtToken")
        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))

        client.post(builder = requestBuilder).apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }


//
//    @Test // Username Taken
//    fun testSignUp1() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "username": "Drake4@uwaterloo.ca",
//            "password": "my-password",
//            "role": "STUDENT",
//            "firstname": "Drake",
//            "lastname": "Rapper"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/signup")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Username Taken! Please use another username.", bodyAsText())
//        }
//    }
//
//    @Test // Password Length too Small
//    fun testSignUp2() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "username": "Drake4@uwaterloo.ca",
//            "password": "my",
//            "role": "STUDENT",
//            "firstname": "Drake",
//            "lastname": "Rapper"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/signup")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Password is too short! Length should be >= 8.", bodyAsText())
//        }
//    }
//
//    @Test // Invalid Role
//    fun testSignUp3() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "username": "Eminem@uwaterloo.ca",
//            "password": "my-password",
//            "role": "RAP-GOD",
//            "firstname": "Marshall",
//            "lastname": "Rapper"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/signup")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Role is invalid! Should be 'TEACHER' or 'STUDENT'.", bodyAsText())
//        }
//    }
//
//    @Test // Invalid Role
//    fun testSignUp4() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "username": "Eminem@uwaterloo.ca",
//            "password": "my-password",
//            "role": "RAP-GOD",
//            "firstname": "Marshall",
//            "lastname": "Rapper"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/signup")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Role is invalid! Should be 'TEACHER' or 'STUDENT'.", bodyAsText())
//        }
//    }
//
//    @Test // Empty Field(s)
//    fun testSignUp5() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "username": "Eminem@uwaterloo.ca",
//            "password": "my-password",
//            "role": "STUDENT",
//            "firstname": "Marshall",
//            "lastname": ""
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/signup")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Some fields are blank!", bodyAsText())
//        }
//    }
//
//    @Test // Valid
//    fun testSignIn1() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "username": "Drake3@uwaterloo.ca",
//            "password": "my-password"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/signin")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//        }
//    }
//
//    @Test // Invalid Username
//    fun testSignIn2() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "username": "obama@uwaterloo.ca",
//            "password": "my-password"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/signin")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Unable to Sign-In! Username not found.", bodyAsText())
//        }
//    }
//
//    @Test // Invalid Password
//    fun testSignIn3() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "username": "Drake3@uwaterloo.ca",
//            "password": "my-passwordd"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/signin")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//        }
//    }
//
//    @Test // Invalid Password
//    fun testSignIn4() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "username": "Drake3@uwaterloo.ca",
//            "password": "my-passwordd"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/signin")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Unable to Sign-In! Username not found.", bodyAsText())
//        }
//    }
//
//    @Test // Valid
//    fun testGetQuestion1() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "questionId": "653204f2b4133a26845ac5ec"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getQuestion")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.get(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.BadRequest, status)
//        }
//    }
//
//    @Test // Invalid
//    fun testGetQuestion2() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "questionId": "65320484d913da7802f4a427"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getQuestion")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.get(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.BadRequest, status)
//            assertEquals("selectedq was null", bodyAsText())
//        }
//    }
//
//    @Test // Invalid
//    fun testAddQuestion1() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "question" : "testQ12",
//            "options": ["op1", "op2"],
//            "responses": [0, 0],
//            "answer": 0
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/addQuestion")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//    @Test // Invalid
//    fun testAddQuestion2() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "question" : "fave colour",
//            "options": ["red", "blue"],
//            "responses": [1, 1, 0],
//            "answer": 0
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/addQuestion")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//    @Test // Valid
//    fun testDeleteQuestion2() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "questionId": "65320484d913da7802f4a427"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getQuestion")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.get(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.BadRequest, status)
////            assertEquals("deletion successful", bodyAsText())
//        }
//    }
//
//    @Test // Valid
//    fun testCreateQuiz1() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "name": "testq1",
//            "state": "HIDDEN",
//            "questions": ["653204f2b4133a26845ac5ec", "6532050b0095e01223740fff"]
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/createQuiz")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.BadRequest, status)
//        }
//    }
//
//    @Test // Invalid
//    fun testCreateQuiz2() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "name": "testq2",
//            "state": "bruh",
//            "questions": ["653204f2b4133a26845ac5ec", "6532050b0095e01223740fff"]
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/createQuiz")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.BadRequest, status)
//        }
//    }
//
//    @Test // Invalid
//    fun testCreateQuiz3() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "name": "testq3",
//            "state": "OPEN",
//            "questions": ["", "6532050b0095e01223740fff"]
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/createQuiz")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.post(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.BadRequest, status)
//        }
//    }
//
//    @Test
//    fun testGetQuiz1() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "quizId": "$quizId"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getQuiz")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.get(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.NotFound, status)
////            assertEquals("State should be OPEN or FINISHED for questions to be visible", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testGetQuiz2() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "quizId": "dne"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getQuiz")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.get(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.NotFound, status)
//        }
//    }
//
//    @Test
//    fun testGetQuiz3() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "quizId": "6531f3cfe9f50d572b6b1ed5"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getQuiz")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.get(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.NotFound, status)
//        }
//    }
//
//    @Test
//    fun testGetQuiz4() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "quizId": "dne"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getQuiz")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.get(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.NotFound, status)
//        }
//    }
//
//    @Test
//    fun testChangeState() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "quizId": "6531fe5d68b6f475b3959362",
//            "newState: "CLOSED"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Patch
//        requestBuilder.url("/changeState")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.patch(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.BadRequest, status)
//        }
//    }
//
//    @Test
//    fun testDeleteQuiz() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "quizId": "6531fc9e13ed7a4cc0bf3bc0"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Delete
//        requestBuilder.url("/deleteQuiz")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//
//
//
//
//
//
//    /* TESTS FOR SPRINT 2 */
//    /*
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//     */
//
//
//    @Test
//    fun testGetUsers() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getUsers")
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    @Test
//    fun testGetStudents() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getStudents")
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    @Test
//    fun testGetTeachers() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getTeachers")
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    @Test
//    fun testGetUserById() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "userId": "6531fc9e13ed7a4cc0bf3bc0"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getUserById")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    @Test
//    fun testIsStudentFromId() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "userId": "6531fc9e13ed7a4cc0bf3bc0"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getStudentFromId")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.NotFound, status)
//        }
//    }
//
//    @Test
//    fun testIsTeacherFromId() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "userId": "6531fc9e13ed7a4cc0bf3bc0"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getTeacherFromId")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.NotFound, status)
//        }
//    }
//
//    @Test
//    fun testIsStudentFromUsername() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "username": "dawson12@uwaterloo.ca"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/isStudentFromUsername")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    @Test
//    fun testIsTeacherFromUsername() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "username": "dawson12@uwaterloo.ca"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/isTeacherFromUsername")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//
//    @Test
//    fun testChangeRole() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "userId": "6531fc9e13ed7a4cc0bf3bc0"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/changeRole")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//
//    @Test
//    fun testChangeFirstName() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "userId": "6531fc9e13ed7a4cc0bf3bc0",
//            "newFirstName": "nfn",
//            "newLastName": ""
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/changeFirstName")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    @Test
//    fun testChangeLastName() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "userId": "6531fc9e13ed7a4cc0bf3bc0",
//            "newFirstName" : "",
//            "newLastName": "nln"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/changeLastName")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    @Test
//    fun testChangeFirstAndLastName() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "userId": "6531fc9e13ed7a4cc0bf3bc0",
//            "newFirstName" : "nfn",
//            "newLastName": "nln"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/changeFirstAndLastName")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    @Test
//    fun testChangeUsername() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "userId": "6531fc9e13ed7a4cc0bf3bc0",
//            "newUsername": "nun
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/changeUsername")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//
//    @Test
//    fun testGetClassSections() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "userId": "6531fc9e13ed7a4cc0bf3bc0",
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getClassSections")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    @Test
//    fun testGetClassSectionJoinableStatus() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "classSectionId": "6531fc9e13ed7a4cc0bf3bc0",
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getClassSectionJoinableStatus")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    @Test
//    fun testGetClassSectionJoinCode() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "classSectionId": "6531fc9e13ed7a4cc0bf3bc0",
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getClassSectionJoinCode")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    @Test
//    fun testMakeClassSectionJoinable() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "classSectionId": "6531fc9e13ed7a4cc0bf3bc0",
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionJoinable")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    @Test
//    fun testMakeClassSectionUnjoinable() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "classSectionId": "6531fc9e13ed7a4cc0bf3bc0",
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionUnjoinable")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    @Test
//    fun testJoinClassSection() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "userId": "6531fc9e13ed7a4cc0bf3bc0",
//            "classSectionId": "6531fc9e13ed7a4cc0cf3bc0"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/joinClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.MethodNotAllowed, status)
//        }
//    }
//
//    // FINISH TESTING BACKEND
//
//    @Test
//    fun testGetQuestionById() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//            "questionId": "6545433dee61c575f1ea8fe8"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getQuestionById")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//    @Test
//    fun testCreateQuestion() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//
//
//        {
//        "quizId": "65559fe931bd4c58d9c8b585",
//        "question": "fave colour?",
//        "options": ["blue", "red", "green"],
//        "answer": 2
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/createQuestion")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//    @Test
//    fun testGetResponsesFromQuestion() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//
//        {
//            "questionId": "6545433dee61c575f1ea8fe8"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getResponsesFromQuestion")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//    @Test
//    fun testGetQuizById() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//
//        {
//            "quizId": "6545450355a422b78e08ad22"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getQuizById")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//    @Test
//    fun testChangeQuizState() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//
//        {
//            "quizId": "$quizId",
//            "newstate": "OPEN"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/changeQuizState")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//    @Test
//    fun testGetQuizQuestions() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//
//        {
//             "quizId": "$quizId"
//        }
//        """
//
//
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getQuizQuestions")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//    @Test
//    fun testGetResponsesForQuestionsInQuiz() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//
//        {
//             "quizId": "$quizId"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getResponsesForQuestionsInQuiz")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//    @Test
//    fun testGetClassSectionById() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getClassSectionById")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//    @Test
//    fun testGetClassSectionByIdInvalid() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "dne"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getClassSectionById")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("ClassSection not found!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testCreateClassSection() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//
//        {
//           "name": "SUSHI"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/getClassSectionById")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//    @Test
//    fun testDeleteClassSection() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Delete
//        requestBuilder.url("/deleteClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("ClassSection Deleted!", bodyAsText())
//
//        }
//    }
//
//    @Test
//    fun testDeleteClassSection2() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Delete
//        requestBuilder.url("/deleteClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("ClassSection not found!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testDeleteClassSection3() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Delete
//        requestBuilder.url("/deleteClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("User must be a Teacher!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testDeleteClassSection4() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Delete
//        requestBuilder.url("/deleteClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Caller must be the teacher of the classSection!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testDeleteClassSection5() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Delete
//        requestBuilder.url("/deleteClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Unable to delete classSection! Database Error.", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testGetQuizzesInClassSection() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getQuizzesInClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//    @Test
//    fun testGetQuizzesInClassSectionInvalid() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "123"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getQuizzesInClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("ClassSection not found!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testGetStudentsInClassSection() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getStudentsInClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }
//
//    @Test
//    fun testGetStudentsInClassSectionInvalid() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "123"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Get
//        requestBuilder.url("/getStudentsInClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("ClassSection not found!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testRemoveStudentFromClassSection() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/removeStudentFromClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("Student has been removed from ClassSection!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testRemoveStudentFromClassSection2() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/removeStudentFromClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("User must be a Teacher!",bodyAsText())
//        }
//    }
//
//    @Test
//    fun testRemoveStudentFromClassSection3() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/removeStudentFromClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("ClassSection not found!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testRemoveStudentFromClassSection4() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/removeStudentFromClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Caller must be the teacher of the classSection!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testRemoveStudentFromClassSection5() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/removeStudentFromClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("ClassSection not found!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testRemoveStudentFromClassSection6() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/removeStudentFromClassSection")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Unable to remove student from classSection! Database Error.", bodyAsText())
//        }
//    }
//
//
//    @Test
//    fun testChangeClassSectionName() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/changeClassSectionName")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("Changed Class Section Name!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testChangeClassSectionName2() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/changeClassSectionName")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("User must be a Teacher!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testChangeClassSectionName3() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/changeClassSectionName")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("ClassSection not found!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testChangeClassSectionName4() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/changeClassSectionName")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Caller must be the teacher of the classSection!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testChangeClassSectionName5() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/changeClassSectionName")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Error in changing name - 1!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testChangeClassSectionName6() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/changeClassSectionName")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Error in changing name - 2!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testChangeClassSectionName7() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/changeClassSectionName")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Error in changing name - 2!", bodyAsText())
//        }
//    }
//
//
//    @Test
//    fun testMakeClassSectionActive() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionActive")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("ClassSection is now active!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testMakeClassSectionActive0() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionActive")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("ClassSection not found!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testMakeClassSectionActive2() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionActive")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Unable to make ClassSection Active!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testMakeClassSectionActive3() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionActive")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("ClassSection not found!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testMakeClassSectionActive4() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionActive")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Caller is not the teacher for this class!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testMakeClassSectionActive5() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionActive")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Unable to parse args!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testMakeClassSectionInactive() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "$classSectionIdVar"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionInactive")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("ClassSection is now inactive!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testMakeClassSectionInactive2() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "notfound"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionInactive")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("ClassSection not found!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testMakeClassSectionInactive3() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "notfound"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionInactive")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("User must be a Teacher!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testMakeClassSectionInactive4() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "notfound"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionInactive")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Caller must be the teacher of the classSection!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testMakeClassSectionInactive5() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "notfound"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionInactive")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Unable to make ClassSection Inactive!", bodyAsText())
//        }
//    }
//
//    @Test
//    fun testMakeClassSectionInactive6() = testApplication {
//        application {
//            configureRouting(userDataSource, questionDataSource, quizDataSource, hashingService, tokenService, tokenConfig, classSectionDataSource, selectionDataSource)
//        }
//
//        val requestBody = """
//        {
//        "classSectionId": "notfound"
//        }
//        """
//
//        val requestBuilder: HttpRequestBuilder = HttpRequestBuilder();
//
//        requestBuilder.method = HttpMethod.Post
//        requestBuilder.url("/makeClassSectionInactive")
//        requestBuilder.setBody(TextContent(requestBody, ContentType.Application.Json))
//
//        client.delete(builder = requestBuilder).apply {
//            assertEquals(HttpStatusCode.Conflict, status)
//            assertEquals("Unable to make ClassSection Inactive!", bodyAsText())
//        }
//    }
//
//
//







}
