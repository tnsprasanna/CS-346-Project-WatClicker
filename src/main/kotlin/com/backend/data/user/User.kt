import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    @BsonId val id: ObjectId =  ObjectId(),
    var username: String,
    var password: String,
    var salt: String,
    var role: String,
    var firstname: String,
    var lastname: String,
    var classSectionList: MutableList<ObjectId>,
)