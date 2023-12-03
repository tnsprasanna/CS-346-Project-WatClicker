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
        fun generateCSVForList(colTitles: MutableList<String>, data: Map<String, MutableList<String>>): String {
            val csvStringBuilder = StringBuilder()

            for (colTitle in colTitles) {
                csvStringBuilder.append("$colTitle, ")
            }
            csvStringBuilder.appendLine("")

            for ((username, values) in data) {
                csvStringBuilder.append("$username, ")

                for (value in values) {
                    csvStringBuilder.append("$value, ")
                }

                csvStringBuilder.appendLine("")
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

        @JvmStatic
        fun getPercentage(numerator: Double, denominator: Double): String {
            if (denominator == 0.0) {
                return "X"
            }

            val percentage = (numerator / denominator) * 100
            val roundedPercentage = String.format("%.2f", percentage)

            return "$roundedPercentage%"
        }
    }
}