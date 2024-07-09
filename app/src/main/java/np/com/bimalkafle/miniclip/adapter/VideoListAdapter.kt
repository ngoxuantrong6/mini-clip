package np.com.bimalkafle.miniclip.adapter

import android.content.Intent
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import np.com.bimalkafle.miniclip.CommentListActivity
import np.com.bimalkafle.miniclip.ProfileActivity
import np.com.bimalkafle.miniclip.R
import np.com.bimalkafle.miniclip.databinding.VideoItemRowBinding
import np.com.bimalkafle.miniclip.model.UserModel
import np.com.bimalkafle.miniclip.model.VideoModel

class VideoListAdapter(
    options: FirestoreRecyclerOptions<VideoModel>
) : FirestoreRecyclerAdapter<VideoModel,VideoListAdapter.VideoViewHolder>(options)  {

    lateinit var currentUserId : String

    inner class VideoViewHolder(private val binding : VideoItemRowBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindVideo(videoModel: VideoModel){
            currentUserId =  FirebaseAuth.getInstance().currentUser?.uid!!

            //bindUserData
            Firebase.firestore.collection("users")
                .document(videoModel.uploaderId)
                .get().addOnSuccessListener {
                    val userModel = it?.toObject(UserModel::class.java)
                    userModel?.apply {
                        binding.usernameView.text = username
                        //bind profilepic
                        Glide.with(binding.profileIcon).load(profilePic)
                            .circleCrop()
                            .apply(
                                RequestOptions().placeholder(R.drawable.icon_profile)
                            )
                            .into(binding.profileIcon)

                        binding.userDetailLayout.setOnClickListener {
                            val intent = Intent(binding.userDetailLayout.context, ProfileActivity::class.java)
                            intent.putExtra("profile_user_id", id )
                            binding.userDetailLayout.context.startActivity(intent)
                        }


                    }
                }

            binding.captionView.text = videoModel.title
            binding.progressBar.visibility = View.VISIBLE


            //bindVideo
            binding.videoView.apply {
                setVideoPath(videoModel.url)
                setOnPreparedListener {
                    binding.progressBar.visibility = View.GONE
                    it.start()
                    it.isLooping = true
                }
                //play pause
                setOnClickListener {
                    if(isPlaying){
                        pause()
                        binding.pauseIcon.visibility = View.VISIBLE
                    }else{
                        start()
                        binding.pauseIcon.visibility = View.GONE
                    }
                }
            }

            binding.likeIcon.apply {
                setOnClickListener {
                    Firebase.firestore.collection("videos")
                        .document(currentUserId)
                        .get()
                        .addOnSuccessListener {
//                            val currentUserModel = it.toObject(UserModel::class.java)!!
                            if(videoModel.likes.contains(currentUserId)){
                                //unlike video
                                videoModel.likes.remove(currentUserId)
                                binding.likeCount.text = videoModel.likes.size.toString()
                                binding.likeIcon.setColorFilter(context.getColor(R.color.white), PorterDuff.Mode.SRC_IN)
                                Firebase.firestore.collection("videos")
                                    .document(videoModel.videoId)
                                    .update("likes", FieldValue.arrayRemove(currentUserId))
                            }else{
                                //like video
                                videoModel.likes.add(currentUserId)
                                binding.likeCount.text = videoModel.likes.size.toString()
                                binding.likeIcon.setColorFilter(context.getColor(R.color.red), PorterDuff.Mode.SRC_IN)
                                Firebase.firestore.collection("videos")
                                    .document(videoModel.videoId)
                                    .update("likes", FieldValue.arrayUnion(currentUserId))
                            }


                        }
                }
                binding.likeCount.text = videoModel.likes.size.toString()
                if(videoModel.likes.contains(currentUserId)) {
                    binding.likeIcon.setColorFilter(context.getColor(R.color.red), PorterDuff.Mode.SRC_IN)
                } else {
                    binding.likeIcon.setColorFilter(context.getColor(R.color.white), PorterDuff.Mode.SRC_IN)
                }
            }

            binding.commentIcon.apply {
                setOnClickListener {
                    val intent = Intent(binding.commentIcon.context, CommentListActivity::class.java)
                    intent.putExtra("video_id", videoModel.videoId)
                    binding.commentIcon.context.startActivity(intent)
                }
                binding.commentCount.text = videoModel.commentCount.toString()
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, model: VideoModel) {
        holder.bindVideo(model)
    }
}
