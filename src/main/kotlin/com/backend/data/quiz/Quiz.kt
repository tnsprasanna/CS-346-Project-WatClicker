data class Quiz(
    val quizId: String = String.toString(),
    val name: String,
    val state: String,
    val questionIds: Array<String> = emptyArray()
)
