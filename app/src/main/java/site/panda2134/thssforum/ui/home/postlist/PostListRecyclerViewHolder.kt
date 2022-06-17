package site.panda2134.thssforum.ui.home.postlist

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.MediaController
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity
import cn.bingoogolapple.photopicker.widget.BGANinePhotoLayout
import com.arges.sepan.argmusicplayer.Models.ArgAudio
import com.bumptech.glide.Glide
import com.github.kittinunf.fuel.core.FuelError
import kotlinx.coroutines.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.PostItemBinding
import site.panda2134.thssforum.models.*
import site.panda2134.thssforum.ui.home.comments.CommentRecyclerViewAdapter
import site.panda2134.thssforum.ui.profile.ProfileUserHomepage
import site.panda2134.thssforum.utils.toTimeAgo

class PostListRecyclerViewHolder(val binding: PostItemBinding, val api: APIWrapper, val recyclerView: RecyclerView,
                                 val activity: Activity, val lifecycleOwner: LifecycleOwner): RecyclerView.ViewHolder(binding.root), BGANinePhotoLayout.Delegate {
    private var post: Post? = null
    var onDeleteCallback: ((post: Post, bindingAdapterPosition: Int)->Unit)? = null
    var mediaController: MediaController? = null
        private set
    private var replyTo: Comment? = null
    private val inputMethodManager = ContextCompat.getSystemService(binding.root.context, InputMethodManager::class.java)!!

    init {
        MainScope().launch {
            while (true) {
                delay(5 * 1000)
                post?.run {
                    binding.postTime.text = postContent.createdAt?.toTimeAgo() ?: return@run
                }
            }
        }
    }

    fun setPost(p: Post) {
        post = p

        binding.likeButton.isChecked = false // default to false
        binding.location.text = ""
        binding.location.visibility = View.GONE
        binding.commentInput.text?.clear()
        binding.commentInput.visibility = View.GONE

        val commentAdapter = (binding.commentView.adapter as CommentRecyclerViewAdapter)
        commentAdapter.postId = p.postContent.id!!
        commentAdapter.clear()

        MainScope().launch {
            commentAdapter.fetchComments()
            val likes = api.getNumOfLikes(p.postContent.id)
            val followingUsers = api.getFollowingUsers()
            withContext(Dispatchers.Main) {
                binding.likeNum.text = likes.count.toString()
                binding.likeButton.isChecked = likes.likedByMe
                binding.followedButton.visibility =
                    if (followingUsers.contains(p.author)) View.VISIBLE else View.GONE
            }
        }

        Glide.with(binding.root).load(p.author.avatar).placeholder(R.drawable.ic_baseline_account_circle_24).into(binding.userAvatar)
        binding.userName.text = p.author.nickname
        binding.removePostButton.visibility =
            if (p.author.uid == api.currentUserId) {
                View.VISIBLE
            } else {
                View.GONE
            }
        p.postContent.location?.let {
            binding.location.visibility = View.VISIBLE
            binding.location.text = it.description
        }
        p.postContent.createdAt?.let {
            binding.postTime.text = it.toTimeAgo()
        }
        val gotoPostAuthorPage = {
            post?.let {
                val intent = Intent(binding.root.context, ProfileUserHomepage::class.java)
                    .putExtra("author", it.author.uid)
                binding.root.context.startActivity(intent)
            }
        }
        binding.userAvatar.setOnClickListener { gotoPostAuthorPage() }
        binding.userName.setOnClickListener { gotoPostAuthorPage() }

        when(p.postContent.type) {
            PostType.normal -> {
                val content = p.postContent.imageTextContent!!
                binding.postTitle.text = content.title
                binding.postContent.text = content.text
                binding.postContent.visibility = View.VISIBLE
                binding.postImages.visibility = View.VISIBLE
                binding.audioPlayer.visibility = View.GONE
                binding.videoPlayerWrapper.visibility = View.GONE
                binding.postImages.data = content.images
                binding.postImages.setDelegate(this)
            }
            PostType.audio -> {
                val content = p.postContent.mediaContent!!
                binding.postTitle.text = content.title
                binding.postContent.visibility = View.GONE
                binding.postImages.visibility = View.GONE
                binding.audioPlayer.visibility = View.VISIBLE
                binding.videoPlayerWrapper.visibility = View.GONE
                binding.audioPlayer.apply {
                    disableNextPrevButtons()
                    setProgressMessage(context.getString(R.string.loading))
                    disableRepeatButton()
                    setPlaylistRepeat(false)
                    playAudioAfterPercent(10)
                    play(ArgAudio.createFromURL(
                        p.author.nickname, p.postContent.mediaContent.title,
                        p.postContent.mediaContent.media[0]
                    ))
                    // 加载完成后不马上开始播放
                    var firstPlayed = false
                    this.setOnPlayingListener {
                        if (!firstPlayed) {
                            firstPlayed = true
                            pause()
                        }
                    }
                }
            }
            PostType.video -> {
                val content = p.postContent.mediaContent!!
                binding.postTitle.text = content.title
                binding.postContent.visibility = View.GONE
                binding.postImages.visibility = View.GONE
                binding.audioPlayer.visibility = View.GONE
                binding.videoPlayerWrapper.visibility = View.VISIBLE
                binding.videoPlayer.setVideoURI(Uri.parse(content.media[0]))
                binding.videoPlayer.seekTo(1)

                mediaController = MediaController(binding.videoPlayerWrapper.context)
                mediaController!!.setAnchorView(binding.videoPlayer)
                binding.videoPlayer.setMediaController(mediaController)
                binding.videoPlayer.setOnPreparedListener {
                    binding.videoPlayer.postDelayed({
                        mediaController!!.hide()
                    },1)
                }
            }
        }

        binding.likeButton.setOnClickListener {
            MainScope().launch(Dispatchers.IO) {
                try {
                    val likeNum = if (binding.likeButton.isChecked) {
                        api.likeThisPost(p.postContent.id).count
                    } else {
                        api.unlikeThisPost(p.postContent.id).count
                    }
                    withContext(Dispatchers.Main) {
                        binding.likeNum.text = likeNum.toString()
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }

        binding.removePostButton.setOnClickListener {
            MainScope().launch(Dispatchers.IO) {
                try {
                    api.deletePost(p.postContent.id)
                    withContext(Dispatchers.Main) {
                        onDeleteCallback?.invoke(p, bindingAdapterPosition)
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }

        binding.shareButton.setOnClickListener {
            val intentBuilder = ShareCompat.IntentBuilder(itemView.context)
            intentBuilder.setType("text/plain").setText(shareString).startChooser()
        }

        commentAdapter.commentClickedHandler = { comment, _ ->
            replyTo = comment
            showCommentEditText()
        }

        binding.commentButton.setOnClickListener {
            showCommentEditText()
        }

        binding.commentInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                MainScope().launch(Dispatchers.IO) {
                    try {
                        api.newComment(post!!.postContent.id!!, NewCommentRequest(
                            binding.commentInput.text.toString(),
                            replyTo?.data?.id
                        ))
                        replyTo = null
                        withContext(Dispatchers.Main) {
                            hideCommentEditText()
                            (binding.commentView.adapter as? CommentRecyclerViewAdapter)?.apply {
                                clear()
                                fetchComments {
                                    Toast.makeText(binding.root.context, R.string.comment_success, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: FuelError) {
                        e.printStackTrace()
                    }
                }
                return@setOnEditorActionListener true
            }
            false
        }

        KeyboardVisibilityEvent.setEventListener(activity, lifecycleOwner) { visible ->
            if (!visible) {
                hideCommentEditText()
            }
        }
    }


    private fun showCommentEditText() {
        binding.commentInput.visibility = View.VISIBLE
        MainScope().launch {
            delay(50)
            binding.commentInput.requestFocus()
            inputMethodManager.showSoftInput(binding.commentInput, 0)
            if (bindingAdapterPosition != bindingAdapter!!.itemCount - 2) {
                recyclerView.scrollToPosition(bindingAdapterPosition + 1)
            }
            delay(50)
            if (bindingAdapterPosition == bindingAdapter!!.itemCount - 2) {
                recyclerView.scrollToPosition(bindingAdapterPosition + 1)
            } else {
                recyclerView.scrollBy(0, -100)
            }
        }
    }

    private fun hideCommentEditText() {
        binding.commentInput.visibility = View.GONE
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    // share的时候只分享文字部分
    private val shareString: String
        get() {
            if(post!!.postContent.type == PostType.normal) {
                if(binding.postContent.text.isNotBlank()) {
                    return "${binding.userName.text} ：\n  标题：${binding.postTitle.text}\n  内容：${binding.postContent.text}\n  时间：${binding.postTime.text}"
                }
                else {
                    return "${binding.userName.text} ：\n  标题：${binding.postTitle.text}\n  时间：${binding.postTime.text}"
                }
            } else{
                return "${binding.userName.text} ：\n  标题：${binding.postTitle.text}\n  链接：${Uri.parse(post!!.postContent.mediaContent!!.media[0])}\n  时间：${binding.postTime.text}"
            }
        }

    override fun onClickNinePhotoItem(
        ninePhotoLayout: BGANinePhotoLayout?,
        view: View?,
        position: Int,
        model: String?,
        models: MutableList<String>?
    ) {
        if (ninePhotoLayout == null) return
        val intentBuilder = BGAPhotoPreviewActivity.IntentBuilder(binding.root.context)
        if (ninePhotoLayout.itemCount == 1) {
            intentBuilder.previewPhoto(ninePhotoLayout.currentClickItem)
        } else {
            intentBuilder.previewPhotos(ninePhotoLayout.data).currentPosition(ninePhotoLayout.currentClickItemPosition)
        }
        startActivity(binding.root.context, intentBuilder.build(), Bundle())
    }

    override fun onClickExpand(
        ninePhotoLayout: BGANinePhotoLayout?,
        view: View?,
        position: Int,
        model: String?,
        models: MutableList<String>?
    ) {
        ninePhotoLayout?.setIsExpand(true)
        ninePhotoLayout?.flushItems()
    }
}