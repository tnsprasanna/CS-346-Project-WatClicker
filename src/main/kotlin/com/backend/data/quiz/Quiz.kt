import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Quiz (
    @BsonId val id: ObjectId = ObjectId(),
    val name: String,
    val state: String,
    val questions: Array<String>
)