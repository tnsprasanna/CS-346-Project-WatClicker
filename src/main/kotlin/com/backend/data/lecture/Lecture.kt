import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Lecture(
    val name: String,
    val active: Boolean,
    @BsonId val id: ObjectId = ObjectId(),
    val quizIds: Array<String> = emptyArray()
)