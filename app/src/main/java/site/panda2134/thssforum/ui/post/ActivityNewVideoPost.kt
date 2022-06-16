package site.panda2134.thssforum.ui.post

//import android.app.Activity
import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.Tag
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.webkit.MimeTypeMap
import android.widget.MediaController
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.PostVideoBinding
import site.panda2134.thssforum.models.MediaPostContent
import site.panda2134.thssforum.models.PostContent
import java.lang.Exception
import java.time.Instant


class ActivityNewVideoPost : ActivityNewPost() {
    private lateinit var binding: PostVideoBinding
    private lateinit var api: APIWrapper
    val videoPath = MutableLiveData<String?>(null)
    val uploading = MutableLiveData(false)

    val progress = MutableLiveData<Int>(0)
    private val tag = "newVideoPost"
    private val permission = Manifest.permission.CAMERA
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        //将从camera拍摄的视频uri上传到oss
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(tag, "OK")
                val uri = result.data?.data ?: return@run
                handleVideoUpload(uri)
            }
            else {
                Log.d(tag, "Oh No")
            }
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        //先申请权限，申请成功后调用camera拍摄视频
        if (it.equals(true)) {
            Log.d(tag, "permission OK")
            try {
                startForResult.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE))
            } catch (e: Exception) {
                Log.d(tag, e.toString())
            }
            Log.d(tag, "Everything is OK!")
        }
        else {
            Log.d(tag, "permission failed")
        }
    }
    private val mActLauncherAlbum =  registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
        //将相册中选择的uri上传至oss
        result?.let {
            handleVideoUpload(result)
        }
    }

    private fun handleVideoUpload(result: Uri) {
        this.lifecycleScope.launch {
            try {
                uploading.value = true
                val uploadedPath = withContext(Dispatchers.IO) {
                    api.uploadFileToOSS(result) { request, currentSize, totalSize ->
                        progress.value = (currentSize * 100 / totalSize).toInt()
                        Log.d(tag, progress.value.toString())
                    }.toString()
                }
                uploading.value = false
                videoPath.value = uploadedPath
                binding.videoPreview.setVideoURI(Uri.parse(uploadedPath))
                Log.d(tag, videoPath.value!!)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun loadDraft() {
        val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
        val draftTitle = pref.getString(getString(R.string.PREF_KEY_VIDEO_TITLE), "")
        val draftPath = pref.getString(getString(R.string.PREF_KEY_VIDEO_PATH), null)
        binding.title.setText(draftTitle)
        videoPath.value = draftPath
        Log.d(tag, "videoPath = ${videoPath.value}")
    }

    private fun saveDraft() {
        val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
        with(pref.edit()) {
            this.putString(getString(R.string.PREF_KEY_VIDEO_TITLE), binding.title.text.toString())
            this.putString(getString(R.string.PREF_KEY_VIDEO_PATH), videoPath.value)
            apply()
        }
    }

    private fun showDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.no_video))
            .setMessage(getString(R.string.please_shot_or_choose_a_video))
            .create()
        alertDialog.show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = APIWrapper(this)
        binding = PostVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.v = this
        binding.selectVideo.setOnClickListener {
            mActLauncherAlbum.launch("video/*")
        }
        loadDraft()
        binding.shootVideo.setOnClickListener {
            requestPermissionLauncher.launch(permission)
        }
        val mediaController = MediaController(this, false)
        mediaController.setAnchorView(binding.videoPreview)
        binding.videoPreview.setMediaController(mediaController)
    }

    override fun finish() {
        super.finish()
        saveDraft()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.send_menu_item -> {
                Log.d(tag, "item clicked")
                if(videoPath.value?.isEmpty() != false) showDialog()
                else {
                    this.lifecycleScope.launch(Dispatchers.IO){
                        Log.d(tag, "onOptionsItemSelected")
                        Log.d(tag, "videoPath: $videoPath")
                        val videoPostContent = MediaPostContent(binding.title.text.toString(), arrayOf(videoPath.value!!))
                        val postContent = PostContent.makeVideoPost(videoPostContent, createdAt = Instant.now())
                        val res = api.newPost(postContent)
                        Log.d(tag, "postId: ${res.id}")
                        withContext(Dispatchers.Main) {
                            binding.title.text.clear()
                            videoPath.value = ""
                            saveDraft() // remove draft
                        }
                        finish()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}