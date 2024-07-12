package np.com.bimalkafle.miniclip.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import np.com.bimalkafle.miniclip.ProfileActivity
import np.com.bimalkafle.miniclip.R
import np.com.bimalkafle.miniclip.databinding.CommentItemRowBinding
import np.com.bimalkafle.miniclip.model.CommentModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class CommentListAdapter(private val commentList: MutableList<CommentModel>) :
    RecyclerView.Adapter<CommentListAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: CommentItemRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindComment(commentModel: CommentModel) {
            binding.usernameView.text = commentModel.username
            binding.commentView.text = commentModel.comment
            Glide.with(binding.profileIcon).load(commentModel.profilePhoto)
                .circleCrop()
                .apply(
                    RequestOptions().placeholder(R.drawable.icon_profile)
                )
                .into(binding.profileIcon)
            binding.commentLayout.setOnClickListener {
                val intent = Intent(binding.commentLayout.context, ProfileActivity::class.java)
                intent.putExtra("profile_user_id", commentModel.userId )
                binding.commentLayout.context.startActivity(intent)
            }
            binding.dateView.text = formatTimestamp(commentModel.createdTime)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = CommentItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bindComment(commentList[position])
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    fun updateCommentList(newCommentList: List<CommentModel>) {
        commentList.clear()
        commentList.addAll(newCommentList)
        notifyDataSetChanged()
    }
    fun formatTimestamp(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val formatter = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }
}
