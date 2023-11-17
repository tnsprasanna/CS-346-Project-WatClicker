package com.backend.routes

import User
import com.backend.data.user.UserDataSource
import com.backend.data.Constants
import com.backend.data.classSection.ClassSection
import com.backend.data.classSection.ClassSectionDataSource
import com.backend.data.quiz.QuizDataSource
import com.backend.data.requests.*
import com.backend.data.responses.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

fun Route.getClassSectionById(
    classSectionDataSource: ClassSectionDataSource
) {
    get("getClassSectionById") {
        val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
            return@get
        }

        val classSection = classSectionDataSource.getClassSectionById(request.classSectionId)?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "ClassSection not found!")
            return@get
        }

        call.respond(
            HttpStatusCode.OK,
            message = ClassSectionResponse(
                id = classSection.id.toString(),
                name = classSection.name,
                teacherId = classSection.teacherId.toString(),
                studentIds = classSection.studentIds.map { it.toString() },
                isActive = classSection.isActive,
                quizIds = classSection.quizIds.map { it.toString() },
                joinCode = classSection.joinCode,
                isJoinable = classSection.isJoinable
            )
        )
    }
}

fun Route.createClassSection(
    classSectionDataSource: ClassSectionDataSource,
    userDataSource: UserDataSource
) {
    authenticate {
        post("createClassSection") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<CreateClassSectionRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserById(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            if (user.role != Constants.TEACHER_ROLE) {
                call.respond(HttpStatusCode.Conflict, "User must be a Teacher!")
                return@post
            }

            val classSection = ClassSection(
                name = request.name,
                isActive = true,
                studentIds = mutableListOf<ObjectId>(),
                teacherId = user.id,
                quizIds = mutableListOf<ObjectId>(),
                joinCode = (1..8).map { ('a'..'z').random() }.joinToString(""),
                isJoinable = request.isJoinable
            )

            val res = classSectionDataSource.createClassSection(classSection)

            if (!res) {
                call.respond(HttpStatusCode.Conflict, "Unable to create classSection! Database Error.");
                return@post
            }

            userDataSource.addClassSectionToUser(userId, classSection.id.toString())

            call.respond(HttpStatusCode.OK, "ClassSection Created!");
        }
    }
}

fun Route.deleteClassSection(
    classSectionDataSource: ClassSectionDataSource,
    userDataSource: UserDataSource
) {
    authenticate{
        delete("deleteClassSection") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@delete
            }

            val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@delete
            }

            val user = userDataSource.getUserById(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@delete
            }

            if (user.role != Constants.TEACHER_ROLE) {
                call.respond(HttpStatusCode.Conflict, "User must be a Teacher!")
                return@delete
            }

            val classSection = classSectionDataSource.getClassSectionById(request.classSectionId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@delete
            }

            if (user.id != classSection.teacherId) {
                call.respond(HttpStatusCode.Conflict, "Caller must be the teacher of the classSection!")
                return@delete
            }

            val res = classSectionDataSource.deleteClassSection(request.classSectionId)
            if (!res) {
                call.respond(HttpStatusCode.Conflict, "Unable to delete classSection! Database Error.");
                return@delete
            }

            userDataSource.removeClassSectionFromUser(userId, request.classSectionId)

            call.respond(HttpStatusCode.OK, "ClassSection Deleted!");
        }
    }
}

fun Route.getQuizzesInClassSection(
    classSectionDataSource: ClassSectionDataSource
) {
    get("getQuizzesInClassSection") {
        val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
            return@get
        }

        val classSection = classSectionDataSource.getClassSectionById(request.classSectionId)?: kotlin.run {
            call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
            return@get
        }

        val quizzes = classSectionDataSource.getQuizzes(request.classSectionId)?: kotlin.run{
            call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
            return@get
        }
        print(quizzes)
        val quizRespList = mutableListOf<QuizResponse>()
        for (q in quizzes) {
            q?: continue
            val temp = QuizResponse(
                id = q.id.toString(),
                name = q.name,
                state = q.state,
                classSectionId = request.classSectionId,
                questionIds = q.questionIds.map { it.toString() }
            )
            quizRespList.add(temp)
        }

        call.respond(
            status = HttpStatusCode.OK,
            message = QuizListResponse(quizRespList)
        )
    }
}

fun Route.getStudentsInClassSection(
    classSectionDataSource: ClassSectionDataSource
) {
    get("getStudentsInClassSection") {
        val request = kotlin.runCatching { call.receiveNullable<ClassSectionIdRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
            return@get
        }

        val classSection = classSectionDataSource.getClassSectionById(request.classSectionId)?: kotlin.run {
            call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
            return@get
        }

        val students = classSectionDataSource.getStudentsInClassSection(request.classSectionId)?: kotlin.run{
            call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
            return@get
        }

        val userRespList = mutableListOf<UserResponse>()
        for (s in students) {
            s?: continue
            val temp = UserResponse(
                id = s.id.toString(),
                username = s.username,
                role = s.role,
                firstname = s.firstname,
                lastname = s.lastname,
                classSectionList = s.classSectionList.map { it.toString() }
            )
            userRespList.add(temp)
        }

        call.respond(
            status = HttpStatusCode.OK,
            message = UserListResponse(userRespList)
        )

    }
}

fun Route.removeStudentFromClassSection(
    classSectionDataSource: ClassSectionDataSource,
    userDataSource: UserDataSource
) {
    authenticate {
        post("removeStudentFromClassSection") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<RemoveStudentFromClassSectionRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserById(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            if (user.role != Constants.TEACHER_ROLE) {
                call.respond(HttpStatusCode.Conflict, "User must be a Teacher!")
                return@post
            }

            val classSection = classSectionDataSource.getClassSectionById(request.classSectionId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@post
            }

            if (user.id != classSection.teacherId) {
                call.respond(HttpStatusCode.Conflict, "Caller must be the teacher of the classSection!")
                return@post
            }

            val resp = classSectionDataSource.removeStudentFromClassSection(request.classSectionId, request.userid)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@post
            }

            if (!resp) {
                call.respond(HttpStatusCode.Conflict, "Unable to remove student from classSection! Database Error.");
                return@post
            }

            userDataSource.removeClassSectionFromUser(request.userid, request.classSectionId)

            call.respond(
                HttpStatusCode.OK,
                message = "Student has been removed from ClassSection!"
            )
        }
    }
}

fun Route.changeClassSectionName(
    classSectionDataSource: ClassSectionDataSource,
    userDataSource: UserDataSource
) {
    authenticate {
        post("changeClassSectionName") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "UserId not retrievable!");
                return@post
            }

            val request = kotlin.runCatching { call.receiveNullable<ChangeClassSectionNameRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "Unable to parse args!")
                return@post
            }

            val user = userDataSource.getUserById(userId) ?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "User not found!")
                return@post
            }

            if (user.role != Constants.TEACHER_ROLE) {
                call.respond(HttpStatusCode.Conflict, "User must be a Teacher!")
                return@post
            }

            val classSection = classSectionDataSource.getClassSectionById(request.classSectionId)?: kotlin.run {
                call.respond(HttpStatusCode.Conflict, "ClassSection not found!")
                return@post
            }

            if (user.id != classSection.teacherId) {
                call.respond(HttpStatusCode.Conflict, "Caller must be the teacher of the classSection!")
                return@post
            }

            val res = classSectionDataSource.changeClassSectionName(request.classSectionId, request.newName)?: kotlin.run{
                call.respond(HttpStatusCode.Conflict, "Error in changing name - 1!")
                return@post
            }

            if (!res) {
                call.respond(HttpStatusCode.Conflict, "Error in changing name - 2!")
                return@post
            }

            call.respond(HttpStatusCode.OK, "Changed Class Section Name!")
        }
    }
}