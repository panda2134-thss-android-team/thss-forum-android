package site.panda2134.thssforum.ui.home.postlist

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.Toast
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

    fun setPost(p: Post) {
        post = p

        MainScope().launch {
            val likes = api.getNumOfLikes(p.postContent.id!!).count
            withContext(Dispatchers.Main) {
                binding.likeNum.text = likes.toString()
            }
        }
        Glide.with(binding.root).load(p.author.avatar).placeholder(R.drawable.ic_baseline_account_circle_24).into(binding.userAvatar)
        binding.userName.text = p.author.nickname
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
                binding.audioPlayer.visibility = View.GONE
                binding.postImages.data = content.images
                binding.postImages.setDelegate(this)
            }
            PostType.audio -> {
                val content = p.postContent.mediaContent!!
                binding.postTitle.text = content.title
                binding.postContent.visibility = View.GONE
                binding.audioPlayer.visibility = View.VISIBLE
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
                binding.postImages.visibility = View.GONE
            }
            PostType.video -> {
                val content = p.postContent.mediaContent!!
                binding.postTitle.text = content.title
                binding.postContent.visibility = View.GONE
                binding.postImages.visibility = View.GONE
                binding.audioPlayer.visibility = View.GONE
                binding.videoPlayer.visibility = View.VISIBLE
                binding.videoPlayer.setVideoURI(Uri.parse(content.media[0]))
//                binding.videoPlayer.setZOrderOnTop(true)
                binding.videoPlayer.seekTo(1)

                val mediaController = MediaController(binding.videoPlayerWrapper.context)
                mediaController.setAnchorView(binding.videoPlayer)
                binding.videoPlayer.setMediaController(mediaController)
            }
        }

        binding.likeButton.setOnClickListener() {
            // 修改个人信息
            MainScope().launch(Dispatchers.IO) {
                try {
                    val user: User = apiService.likeThisPost(post_id)

                    withContext(Dispatchers.Main) {
                        binding.myName.text = user.nickname.toEditable()
                        binding.myIntro.text = user.intro.toEditable()
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