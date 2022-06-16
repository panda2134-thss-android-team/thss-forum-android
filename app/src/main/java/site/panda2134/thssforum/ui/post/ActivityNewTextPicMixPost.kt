package site.panda2134.thssforum.ui.post

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.PostTextPicMixBinding
import site.panda2134.thssforum.databinding.PostVideoBinding
import site.panda2134.thssforum.models.MediaPostContent
import site.panda2134.thssforum.models.PostContent
import java.lang.Exception
import java.time.Instant
class ActivityNewTextPicMixPost : ActivityNewPost() {
    // TODO
    private lateinit var binding: PostTextPicMixBinding
    private lateinit var api: APIWrapper
    private var imagePath: String? = null
    private val tag = "newVideoPost"
    private val permission = Manifest.permission.CAMERA
    private val alertDialog: AlertDialog
        get() = AlertDialog.Builder(this)
            .setTitle(getString(R.string.no_photo))
            .setMessage(R.string.please_shot_or_choose_a_photo)
            .create()
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        //将从camera拍摄的视频uri上传到oss
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(tag, "OK")
                val uri = result.data?.data
                uri?.let{
                    this.lifecycleScope.launch(Dispatchers.IO) {
                        imagePath = api.uploadFileToOSS(uri).toString()
                        Log.d(tag, "imagePath = $imagePath")
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
                startForResult.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
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
                imagePath = api.uploadFileToOSS(result).toString()
                Log.d(tag, imagePath!!)
            }
        }
    }

    private fun loadDraft() {
        val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
        val draftTitle = pref.getString(getString(R.string.PREF_KEY_VIDEO_TITLE), "")
        val draftPath = pref.getString(getString(R.string.PREF_KEY_VIDEO_PATH), "")
        binding.title.setText(draftTitle)
        imagePath = draftPath
        Log.d(tag, "imagePath = $imagePath")
    }

    private fun saveDraft() {
        val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
        with(pref.edit()) {
            this.putString(getString(R.string.PREF_KEY_VIDEO_TITLE), binding.title.text.toString())
            this.putString(getString(R.string.PREF_KEY_VIDEO_PATH), imagePath)
            apply()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = APIWrapper(this)
        binding = PostTextPicMixBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.selectPicture.setOnClickListener {
            Log.d(tag, "Click")
            mActLauncherAlbum.launch("image/*")
        }
        loadDraft()
        binding.shootPicture.setOnClickListener {
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
                if(imagePath == "") {
                    alertDialog.show()
                }
                else {
                    this.lifecycleScope.launch(Dispatchers.IO){
                        Log.d(tag, "imagePath: $imagePath")
                        val videoPostContent = MediaPostContent(binding.title.text.toString(), arrayOf(imagePath!!))
                        val postContent = PostContent.makeVideoPost(videoPostContent, createdAt = Instant.now())
                        val res = api.newPost(postContent)
                        val post = api.getPostDetails(res.id)
                        Log.d(tag, "postId: ${res.id}")
                        Log.d(tag, "title: ${post.postContent.mediaContent?.title}")
                        binding.title.text?.clear()
                        imagePath = ""
                        finish()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}