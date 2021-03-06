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
import com.google.gson.JsonObject
import site.panda2134.thssforum.api.gsonFireObject

class ActivityNewVideoPost: ActivityNewPostWithDraft<ActivityNewVideoPost.VideoDraftHolder>() {
    data class VideoDraftHolder (
        override val title: String,
        val videoPath: String?
    ): ActivityNewPostWithDraft.DraftHolder() {
        override fun getActivityClassName(): String = ActivityNewVideoPost::class.java.name
    }

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

    private val noTitleDialog: AlertDialog
        get() = AlertDialog.Builder(this)
            .setTitle(getString(R.string.no_title))
            .setMessage(R.string.please_add_a_title)
            .create()
    private val noVideoDialog: AlertDialog
        get() = AlertDialog.Builder(this)
            .setTitle(getString(R.string.no_video))
            .setMessage(R.string.please_shot_or_choose_a_video)
            .create()
    val progressPercentage = MutableLiveData<Int>(0)
    private val tag = "newVideoPost"
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        //??????camera???????????????uri?????????oss
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
        //???????????????????????????????????????camera????????????
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
        //?????????????????????uri?????????oss
        result?.let {
            handleVideoUpload(result)
        }
    }

    private fun handleVideoUpload(result: Uri) {
        this.lifecycleScope.launch {
            try {
                uploading.value = true
                val uploadedPath = withContext(Dispatchers.IO) {
                    api.uploadFileToOSS(result) { _, currentSize, totalSize ->
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = APIWrapper(this)
        binding = PostVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.v = this
        draftHolder?.let { holder ->
            binding.title.setText(holder.title)
            videoPath.value = holder.videoPath
        }
        binding.selectVideo.setOnClickListener {
            mActLauncherAlbum.launch("video/*")
        }
        binding.shootVideo.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        initAddLocation()
        val mediaController = MediaController(this, false)
        mediaController.setAnchorView(binding.videoPreview)
        binding.videoPreview.setMediaController(mediaController)
    }

    override fun saveDraft() {
        draftHolder = VideoDraftHolder(
            binding.title.text.toString(),
            videoPath.value
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.send_menu_item -> {
                Log.d(tag, "item clicked")
                if(binding.title.text.toString() == "") {
                    noTitleDialog.show()
                }
                else if(videoPath.value?.isEmpty() != false) {
                    noVideoDialog.show()
                }
                else {
                    this.lifecycleScope.launch(Dispatchers.IO){
                        Log.d(tag, "onOptionsItemSelected")
                        Log.d(tag, "videoPath: $videoPath")
                        val videoPostContent = MediaPostContent(binding.title.text.toString(), arrayOf(videoPath.value!!))
                        val postContent = PostContent.makeVideoPost(videoPostContent, createdAt = Instant.now(), location = location)
                        val res = api.newPost(postContent)
                        Log.d(tag, "postId: ${res.id}")
                        withContext(Dispatchers.Main) {
                            binding.title.text.clear()
                            videoPath.value = ""
                            Toast.makeText(this@ActivityNewVideoPost, R.string.post_success, Toast.LENGTH_SHORT).show()
                            isPostSent = true
                            finish()
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun deserializeDraftJson(j: JsonObject): VideoDraftHolder = gsonFireObject.fromJson(j, VideoDraftHolder::class.java)
    override fun serializeDraftJson(holder: VideoDraftHolder): JsonObject = gsonFireObject.run {
        fromJson(toJson(holder), JsonObject::class.java)
    }
}