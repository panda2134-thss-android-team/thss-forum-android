package site.panda2134.thssforum.ui.post

//import android.app.Activity
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.PostVideoBinding
import site.panda2134.thssforum.models.Location
import site.panda2134.thssforum.models.MediaPostContent
import site.panda2134.thssforum.models.PostContent
import java.lang.Exception
import java.time.Instant
import android.view.View
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption

class ActivityNewVideoPost : ActivityNewPost() {
    private lateinit var binding: PostVideoBinding
    private lateinit var api: APIWrapper
    val videoPath = MutableLiveData<String?>(null)
    val uploading = MutableLiveData(false)
    lateinit var locationClient: AMapLocationClient
    private var location: Location? = null

    private val requestLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            binding.addLocation.isEnabled = ! it.values.contains(false)
        }

    private fun initAddLocation() {
        requestLocationPermissions.launch(listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE).toTypedArray())
        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)

        locationClient = AMapLocationClient(applicationContext)
        locationClient.setLocationListener {
            Log.d("AMAP", it.address)
            binding.addLocation.visibility = View.GONE
            binding.location.visibility = View.VISIBLE
            binding.location.text = it.address
            location = Location(it.address, it.longitude.toBigDecimal(), it.latitude.toBigDecimal())
        }
        locationClient.setLocationOption(
            AMapLocationClientOption()
            .apply {
                locationPurpose = AMapLocationClientOption.AMapLocationPurpose.Transport
            })

        binding.addLocation.setOnClickListener {
            locationClient.stopLocation()
            locationClient.startLocation()
        }
    }

    val progressPercentage = MutableLiveData<Int>(0)
    private val tag = "newVideoPost"
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
                        progressPercentage.value = (currentSize * 100 / totalSize).toInt()
                        Log.d(tag, progressPercentage.value.toString())
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
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        initAddLocation()
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
                            Toast.makeText(this@ActivityNewVideoPost, R.string.post_success, Toast.LENGTH_SHORT).show()
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