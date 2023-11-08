package com.backend.data
class Constants {
    companion object {
        const val TEACHER_ROLE: String = "TEACHER";
        const val STUDENT_ROLE: String = "STUDENT";
        const val HIDDEN = "HIDDEN";
        const val OPEN = "OPEN";
        const val CLOSED = "CLOSED";
        const val FINISHED = "FINISHED";
        val QUIZ_STATES = arrayOf(HIDDEN, OPEN, CLOSED, FINISHED);
    }
}