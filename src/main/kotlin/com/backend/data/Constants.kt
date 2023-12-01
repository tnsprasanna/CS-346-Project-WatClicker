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

        @JvmStatic
        fun generateCSVForList(data: Map<String, MutableList<Double>>): String {
            val csvStringBuilder = StringBuilder()

            for ((username, values) in data) {
                csvStringBuilder.append("$username, ")

                for (value in values) {
                    val transformedValue = if (value == -1.0) "X" else "%.2f%%".format(value * 100)
                    csvStringBuilder.append("$transformedValue, ")
                }

                csvStringBuilder.appendln(",")
            }

            return csvStringBuilder.toString()
        }

        @JvmStatic
        fun generateCSVForOne(data: Map<String, Double>): String {
            val csvStringBuilder = StringBuilder()

            for ((username, value) in data) {
                csvStringBuilder.append("$username, $value")
            }

            return csvStringBuilder.toString()
        }
    }
}