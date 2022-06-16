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
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    var videoPath: String? = ""
    private val tag = "newVideoPost"
    private val permission = Manifest.permission.CAMERA
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        //将从camera拍摄的视频uri上传到oss
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(tag, "OK")
                val uri = result.data?.data
                uri?.let{
                    this.lifecycleScope.launch(Dispatchers.IO) {
                        videoPath = api.uploadFileToOSS(uri).toString()
                        Log.d(tag, "videoPath = $videoPath")
                    }
                }
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
            this.lifecycleScope.launch(Dispatchers.IO) {
                videoPath = api.uploadFileToOSS(result).toString()
                Log.d(tag, videoPath!!)
            }
        }
    }

    private fun loadDraft() {
        val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
        val draftTitle = pref.getString(getString(R.string.PREF_KEY_VIDEO_TITLE), "")
        val draftPath = pref.getString(getString(R.string.PREF_KEY_VIDEO_PATH), "")
        binding.title.setText(draftTitle)
        videoPath = draftPath
        Log.d(tag, "videoPath = $videoPath")
    }

    private fun saveDraft() {
        val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
        with(pref.edit()) {
            this.putString(getString(R.string.PREF_KEY_VIDEO_TITLE), binding.title.text.toString())
            this.putString(getString(R.string.PREF_KEY_VIDEO_PATH), videoPath)
            apply()
        }
    }

    private fun showDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("还没有视频")
            .setMessage("请拍摄或从相册选择一个视频👀")
            .setPositiveButton("好的", null)
            .create()
        alertDialog.show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = APIWrapper(this)
        binding = PostVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.selectVideo.setOnClickListener {
            Log.d(tag, "Click")
            mActLauncherAlbum.launch("video/*")
        }
        loadDraft()
        binding.activityNewVideoPost = this
        binding.shootVideo.setOnClickListener {
            requestPermissionLauncher.launch(permission)
        }
    }

    override fun finish() {
        super.finish()
        saveDraft()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.send_menu_item -> {
                Log.d(tag, "item clicked")
                if(videoPath == "") showDialog()
                else {
                    this.lifecycleScope.launch(Dispatchers.IO){
                        Log.d(tag, "onOptionsItemSelected")
                        Log.d(tag, "videoPath: $videoPath")
                        val videoPostContent = MediaPostContent(binding.title.text.toString(), arrayOf(videoPath!!))
                        val postContent = PostContent.makeVideoPost(videoPostContent, createdAt = Instant.now())
                        val res = api.newPost(postContent)
                        val post = api.getPostDetails(res.id)
                        Log.d(tag, "postId: ${res.id}")
                        Log.d(tag, "title: ${post.postContent.mediaContent?.title}")
                        binding.title.setText("")
                        videoPath = ""
                        finish()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}