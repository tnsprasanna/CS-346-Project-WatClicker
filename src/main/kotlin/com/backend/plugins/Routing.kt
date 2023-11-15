package com.backend.plugins

import com.backend.*
import com.backend.data.classSection.ClassSectionDataSource
import com.backend.data.quiz.QuizDataSource
import com.backend.routes.authenticate
import com.backend.data.user.UserDataSource
import com.backend.routes.*
import com.backend.data.questions.QuestionDataSource
import com.backend.routes.getSecretInfo
import com.backend.security.hashing.HashingService
import com.backend.security.token.TokenConfig
import com.backend.security.token.TokenService
import com.backend.routes.signIn
import com.backend.routes.signUp
import com.backend.routes.deleteQuestion
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.backend.data.selection.SelectionDataSource


fun Application.configureRouting(
    userDataSource: UserDataSource,
    questionDataSource: QuestionDataSource,
    quizDataSource: QuizDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    classSectionDataSource: ClassSectionDataSource,
    selectionDataSource: SelectionDataSource
) {
    routing {
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        signUp(userDataSource, hashingService)
        authenticate()
        getSecretInfo(userDataSource)
        getUsers(userDataSource)
        getStudents(userDataSource)
        getTeachers(userDataSource)

        getQuestionById(questionDataSource)
        createQuestion(userDataSource, classSectionDataSource, quizDataSource, questionDataSource)
        deleteQuestion(userDataSource, classSectionDataSource, quizDataSource, questionDataSource)
        // APIs for Editing Quiz Fields

        createQuiz(userDataSource, classSectionDataSource, quizDataSource)
        getQuizById(quizDataSource)
        changeQuizState(userDataSource, classSectionDataSource, quizDataSource)
        deleteQuiz(userDataSource, classSectionDataSource, quizDataSource)
        getQuizQuestions(quizDataSource, questionDataSource)

        getUserById(userDataSource)
        isStudentFromId(userDataSource)
        isStudentFromUsername(userDataSource)
        isTeacherFromId(userDataSource)
        isTeacherFromUsername(userDataSource)
        deleteUser(userDataSource)
        changeRole(userDataSource)
        changeFirstName(userDataSource)
        changeLastName(userDataSource)
        changeFirstAndLastName(userDataSource)
        changeUsername(userDataSource)

        getClassSectionById(classSectionDataSource)
        createClassSection(classSectionDataSource, userDataSource)
        deleteClassSection(classSectionDataSource, userDataSource)
        getQuizzesInClassSection(classSectionDataSource)
        getStudentsInClassSection(classSectionDataSource)
        removeStudentFromClassSection(classSectionDataSource, userDataSource)
        changeClassSectionName(classSectionDataSource, userDataSource)

        getClassSections(userDataSource, classSectionDataSource)
        getClassSectionJoinableStatus(classSectionDataSource)
        getClassSectionJoinCode(classSectionDataSource, userDataSource)
        makeClassSectionJoinable(classSectionDataSource, userDataSource)
        makeClassSectionUnjoinable(classSectionDataSource, userDataSource)
        makeClassSectionActive(classSectionDataSource, userDataSource)
        makeClassSectionInactive(classSectionDataSource, userDataSource)
        joinClassSection(userDataSource, classSectionDataSource)

        getSelectionById(selectionDataSource)
        createSelection(selectionDataSource, userDataSource, questionDataSource)
        deleteSelection(selectionDataSource, userDataSource, questionDataSource)
        editSelection(selectionDataSource, userDataSource, questionDataSource)


        get("") {
            call.respond(HttpStatusCode.OK, "CS 346 Proj Backend is Running!")
        }
    }
}
