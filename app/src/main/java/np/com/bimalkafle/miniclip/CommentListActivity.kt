package np.com.bimalkafle.miniclip

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import np.com.bimalkafle.miniclip.adapter.CommentListAdapter
import np.com.bimalkafle.miniclip.databinding.ActivityCommentListBinding
import np.com.bimalkafle.miniclip.model.CommentModel
import np.com.bimalkafle.miniclip.model.UserModel
import np.com.bimalkafle.miniclip.model.VideoModel

class CommentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentListBinding
    private lateinit var adapter: CommentListAdapter
    private var commentList = mutableListOf<CommentModel>()
    private val firestore = Firebase.firestore
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    private lateinit var videoId: String
    lateinit var profileUserModel : UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoId = intent.getStringExtra("video_id") ?: return

        adapter = CommentListAdapter(commentList)
        println("qua set adapter ${commentList.size}")
        binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.commentRecyclerView.adapter = adapter

        binding.postCommentButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                postComment(videoId)
            }
        }

        getProfileDataFromFirebase()
        loadComments(videoId)
    }

    suspend fun getCommentCount(postId: String): Int {
        val allDocs = firestore.collection("videos")
            .document(postId)
            .collection("comments")
            .get()
            .await()
        println("Number of documents: ${allDocs.size()}")
        return allDocs.size()
    }

    suspend fun postComment(videoId: String) {
        val commentText = binding.commentEditText.text.toString()
        if (commentText.isNotEmpty()) {
            val len = getCommentCount(videoId)
            println("len neeee $len")
            val commentModel = CommentModel(
                id = "Comment $len",
                userId = currentUserId,
                username = profileUserModel.username,
                comment = commentText,
                profilePhoto = profileUserModel.profilePic,
                createdTime = Timestamp.now()
            )
            firestore.collection("videos")
                .document(videoId)
                .collection("comments")
                .document("Comment $len")
                .set(commentModel.toJson()).addOnSuccessListener {
                    binding.commentEditText.text.clear()
                }.await()
            val doc = firestore.collection("videos").document(videoId).get().await()
            val commentCount = doc.getLong("commentCount") ?: 0L
            firestore.collection("videos").document(videoId).update(
                "commentCount", commentCount + 1
            ).await()
        }
    }

    private fun loadComments(videoId: String) {
        firestore.collection("videos")
            .document(videoId)
            .collection("comments")
            .orderBy("createdTime", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null) {
                    val comments = querySnapshot.documents.map { document ->
                        CommentModel.fromSnap(document)
                    }
                    commentList = comments.toMutableList()
                    adapter.updateCommentList(commentList)
                }
            }
    }

    fun getProfileDataFromFirebase(){
        Firebase.firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener {
                profileUserModel = it.toObject(UserModel::class.java)!!
            }

    }
}
