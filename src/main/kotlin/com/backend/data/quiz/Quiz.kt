import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId


data class Quiz(
    @BsonId val id: ObjectId = ObjectId(),
    var name: String,
    var state: String,
    val classSectionId: ObjectId,
    var questionIds: MutableList<ObjectId>
)