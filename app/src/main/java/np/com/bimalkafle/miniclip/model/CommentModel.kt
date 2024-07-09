package np.com.bimalkafle.miniclip.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class CommentModel (
    var id : String = "",
    var userId : String = "",
    var username : String = "",
    var comment : String = "",
    var profilePhoto : String = "",
    var createdTime : Timestamp = Timestamp.now(),
) {

    fun toJson(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "username" to username,
            "comment" to comment,
            "profilePhoto" to profilePhoto,
            "createdTime" to createdTime,
        )
    }
    companion object {
        fun fromSnap(snap: DocumentSnapshot): CommentModel {
            val snapshot = snap.data as Map<String, Any>
            return CommentModel(
                id = snapshot["id"] as String,
                userId = snapshot["userId"] as String,
                username = snapshot["username"] as String,
                comment = snapshot["comment"] as String,
                profilePhoto = snapshot["profilePhoto"] as String,
                createdTime = snapshot["createdTime"] as Timestamp,
            )
        }
    }
}