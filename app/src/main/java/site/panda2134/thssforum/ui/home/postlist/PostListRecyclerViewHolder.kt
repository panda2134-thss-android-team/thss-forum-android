package site.panda2134.thssforum.ui.home.postlist

import android.location.Location
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.api.APIService
import site.panda2134.thssforum.api.downloadImage
import site.panda2134.thssforum.databinding.PostItemBinding
import site.panda2134.thssforum.models.Post
import site.panda2134.thssforum.models.PostType

class PostListRecyclerViewHolder(val binding: PostItemBinding, val api: APIService): RecyclerView.ViewHolder(binding.root) {
    private var post: Post? = null
    fun setPost(p: Post) {
        post = p

        MainScope().launch {
            val bmp = downloadImage(p.author.avatar)
            val likes = api.getNumOfLikes(p.postContent.id!!).count
            withContext(Dispatchers.Main) {
                binding.userAvatar.setImageBitmap(bmp)
                binding.likeNum.text = likes.toString()
            }
        }
        binding.userName.text = p.author.nickname
        p.postContent.location?.let {
            binding.location.text = it.description
        }
        when(p.postContent.type) {
            PostType.normal -> {
                val content = p.postContent.imageTextContent!!
                binding.postTitle.text = content.title
                binding.postContent.text = content.text
            }
            else -> println("not implemented")
        }
    }
}