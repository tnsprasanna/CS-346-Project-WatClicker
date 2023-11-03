package com.backend.routes

import User
import com.backend.data.Constants
import com.backend.data.requests.SignInRequest
import com.backend.data.requests.SignUpRequest
import com.backend.data.responses.AuthResponse
import com.backend.data.responses.UserListResponse
import com.backend.data.responses.UserResponse
import com.backend.data.user.UserDataSource
import com.backend.security.hashing.HashingService
import com.backend.security.hashing.SaltedHash
import com.backend.security.token.TokenClaim
import com.backend.security.token.TokenConfig
import com.backend.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUp(
    userDataSource: UserDataSource,
    hashingService: HashingService
) {
    post("signup") {
        val request = kotlin.runCatching { call.receiveNullable<SignUpRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val areFieldsBlank = request.username.isBlank()
                || request.password.isBlank()
                || request.role.isBlank()
                || request.firstname.isBlank()
                || request.lastname.isBlank();
        val isPwTooShort = request.password.length < 8;
        val isRoleInvalid = (request.role != Constants.TEACHER_ROLE) && (request.role != Constants.STUDENT_ROLE);
        val existingUser = userDataSource.getUserByUsername(request.username)

        if (areFieldsBlank) { // All user fields must be filled in
            call.respond(HttpStatusCode.Conflict, "Some fields are blank!");
            return@post
        } else if (isPwTooShort) { // Password should be long enough
            call.respond(HttpStatusCode.Conflict, "Password is too short! Length should be >= 8.");
            return@post
        } else if (isRoleInvalid) { // Should be a valid role
            call.respond(HttpStatusCode.Conflict, "Role is invalid! Should be 'TEACHER' or 'STUDENT'.")
            return@post
        } else if (existingUser != null) { // Username must be available
            call.respond(HttpStatusCode.Conflict, "Username Taken! Please use another username.")
            return@post
        }

        // Generate Salt and Hash for new user
        val saltedHash = hashingService.generateSaltedHash(request.password)

        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt,
            role = request.role,
            firstname = request.firstname,
            lastname = request.lastname,
            classSectionList = emptyList(),
        );

        // Try to insert new user into DB
        val wasAcknowledged = userDataSource.insertUser(user)

        if (!wasAcknowledged) { // Error inserting new user into DB
            call.respond(HttpStatusCode.Conflict, "Unable to create user! Database Error.");
            return@post
        }

        call.respond(HttpStatusCode.OK, "User Created!");
    }
}

fun Route.signIn(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signin") {
        val request = kotlin.runCatching { call.receiveNullable<SignInRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByUsername(request.username)
        if (user == null) { // Username must exist
            call.respond(HttpStatusCode.Conflict, "Unable to Sign-In! Username not found.")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (!isValidPassword) { // Handle incorrect password
            call.respond(HttpStatusCode.Conflict, "Unable to Sign-In! Password Incorrect.")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token
            )
        )
    }
}

fun Route.authenticate() {
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getSecretInfo(
    userDataSource: UserDataSource
) {
    authenticate {
        get("secret") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class)
            if (userId == null) {
                call.respond(HttpStatusCode.Conflict, "UserId not retrievable!");
                return@get
            }

            val user = userDataSource.getUserById(userId)
            if (user == null) {
                call.respond(HttpStatusCode.Conflict, "User not sound!");
                return@get
            }

            call.respond(
                status = HttpStatusCode.OK,
                message = UserResponse(user.id.toString(),
                    user.username,
                    user.role,
                    user.firstname,
                    user.lastname,
                    user.classSectionList.map { it.toString() })
            )
        }
    }
}

