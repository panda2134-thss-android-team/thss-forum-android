package site.panda2134.thssforum.ui.home.postlist

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity
import cn.bingoogolapple.photopicker.widget.BGANinePhotoLayout
import com.arges.sepan.argmusicplayer.Models.ArgAudio
import com.bumptech.glide.Glide
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIService
import site.panda2134.thssforum.databinding.PostItemBinding
import site.panda2134.thssforum.models.Post
import site.panda2134.thssforum.models.PostType
import java.util.*

class PostListRecyclerViewHolder(val binding: PostItemBinding, val api: APIService): RecyclerView.ViewHolder(binding.root), BGANinePhotoLayout.Delegate {
    private var post: Post? = null
    var onDeleteCallback: ((post: Post, bindingAdapterPosition: Int)->Unit)? = null
    var mediaController: MediaController? = null
        private set

    fun setPost(p: Post) {
        post = p

        binding.likeButton.isChecked = false // default to false
        MainScope().launch {
            val likes = api.getNumOfLikes(p.postContent.id!!)
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
            binding.location.text = it.description
        }
        p.postContent.createdAt?.let {
            val timeAgoMessages = AppCompatDelegate.getApplicationLocales()[0].let { firstLocale ->
                TimeAgoMessages.Builder().withLocale(
                     Locale.forLanguageTag(firstLocale?.language ?: "en")
                )
            }.build()

            binding.postTime.text = TimeAgo.using(it.toEpochMilli(), timeAgoMessages)
        }
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
            }
        }

        binding.likeButton.setOnClickListener {
            MainScope().launch(Dispatchers.IO) {
                try {
                    val likeNum = if (binding.likeButton.isChecked) {
                        api.likeThisPost(p.postContent.id!!).count
                    } else {
                        api.unlikeThisPost(p.postContent.id!!).count
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
                    api.deletePost(p.postContent.id!!)
                    withContext(Dispatchers.Main) {
                        onDeleteCallback?.invoke(p, bindingAdapterPosition)
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
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