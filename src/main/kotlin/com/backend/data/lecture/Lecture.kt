import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Lecture(
    @BsonId val id: ObjectId = ObjectId(),

    val name: String,
    val active: Boolean,
    val studentIds: List<ObjectId>,
    val teacherId: ObjectId,
    val quizIds: List<ObjectId>,
    val joinCode: String,
    val isJoinable: Boolean
)