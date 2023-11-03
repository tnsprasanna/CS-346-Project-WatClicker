import kotlinx.serialization.Serializable

@Serializable
data class Selection(
    val selectionId: String,
    val questionId: String,
    val studentId: String,
    val selectedOption: Int,
    val isCorrect: Boolean?
)