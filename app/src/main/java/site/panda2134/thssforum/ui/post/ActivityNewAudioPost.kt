package site.panda2134.thssforum.ui.post

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.arges.sepan.argmusicplayer.Models.ArgAudio
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.api.gsonFireObject
import site.panda2134.thssforum.databinding.PostAudioBinding
import site.panda2134.thssforum.models.Location
import site.panda2134.thssforum.models.MediaPostContent
import site.panda2134.thssforum.models.PostContent
import java.lang.Exception
import java.time.Instant

class ActivityNewAudioPost : ActivityNewPostWithDraft<ActivityNewAudioPost.AudioDraftHolder>() {
    data class AudioDraftHolder(
        override val title: String,
        val audioPath: String?
    ): ActivityNewPostWithDraft.DraftHolder() {
        override fun getActivityClassName(): String = ActivityNewAudioPost::class.java.name
    }

    private lateinit var binding: PostAudioBinding
    private lateinit var api: APIWrapper
    val audioPath = MutableLiveData<String?>(null)
    val uploading = MutableLiveData(false)
    lateinit var locationClient: AMapLocationClient
    private var location: Location? = null
    private val permissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE)

    private val requestLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            binding.addLocation.isEnabled = ! it.values.contains(false)
        }

    private fun initAddLocation() {
        requestLocationPermissions.launch(permissions.toTypedArray())

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
    private val noAudioDialog: AlertDialog
        get() = AlertDialog.Builder(this)
            .setTitle(getString(R.string.no_audio))
            .setMessage(R.string.please_record_or_choose_an_audio)
            .create()
    val progress = MutableLiveData<Int>(0)
    private val tag = "newAudioPost"
    private val permission = Manifest.permission.RECORD_AUDIO
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        //??????????????????uri?????????oss
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(tag, "OK")
                val uri = result.data?.data ?: return@run
                handleAudioUpload(uri)
            }
            else {
                Log.d(tag, "Oh No")
            }
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        //????????????????????????????????????????????????
        if (it.equals(true)) {
            Log.d(tag, "permission OK")
            try {
                startForResult.launch(Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION))
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
            handleAudioUpload(result)
        }
    }

    private fun playAudio() {
        binding.audioPreview.apply {
            disableNextPrevButtons()
            setProgressMessage(context.getString(R.string.loading))
            disableRepeatButton()
            setPlaylistRepeat(false)
            playAudioAfterPercent(10)
            play(
                ArgAudio.createFromURL(
                getString(R.string.uploaded_preview),
                binding.title.text.toString(),
                audioPath.value
            ))
            // ????????????????????????????????????
            var firstPlayed = false
            this.setOnPlayingListener {
                if (!firstPlayed) {
                    firstPlayed = true
                    pause()
                }
            }
        }
    }
    private fun handleAudioUpload(result: Uri) {
        this.lifecycleScope.launch {
            try {
                uploading.value = true
                val uploadedPath = withContext(Dispatchers.IO) {
                    api.uploadFileToOSS(result) { _, currentSize, totalSize ->
                        progress.value = (currentSize * 100 / totalSize).toInt()
                        Log.d(tag, progress.value.toString())
                    }.toString()
                }
                uploading.value = false
                audioPath.value = uploadedPath
                playAudio()
                Log.d(tag, audioPath.value!!)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = APIWrapper(this)
        binding = PostAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.v = this
        draftHolder?.let { holder ->
            binding.title.setText(holder.title)
            audioPath.value = holder.audioPath
        }
        binding.selectAudio.setOnClickListener {
            mActLauncherAlbum.launch("audio/*")
        }
        binding.recordAudio.setOnClickListener {
            requestPermissionLauncher.launch(permission)
        }
        initAddLocation()
        audioPath.value?.let{
            playAudio()
        }
    }

    override fun saveDraft() {
        draftHolder = AudioDraftHolder(binding.title.text.toString(), audioPath.value)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.send_menu_item -> {
                Log.d(tag, "item clicked")
                if (binding.title.text.toString() == "") noTitleDialog.show()
                else if (audioPath.value?.isEmpty() != false) noAudioDialog.show()
                else {
                    this.lifecycleScope.launch(Dispatchers.IO){
                        Log.d(tag, "onOptionsItemSelected")
                        Log.d(tag, "audioPath: $audioPath")
                        val audioPostContent = MediaPostContent(binding.title.text.toString(), arrayOf(audioPath.value!!))
                        val postContent = PostContent.makeAudioPost(audioPostContent, createdAt = Instant.now(), location = location)
                        val res = api.newPost(postContent)
                        Log.d(tag, "postId: ${res.id}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ActivityNewAudioPost, R.string.post_success, Toast.LENGTH_SHORT).show()
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

    override fun deserializeDraftJson(j: JsonObject): AudioDraftHolder = gsonFireObject.fromJson(j, AudioDraftHolder::class.java)
    override fun serializeDraftJson(holder: AudioDraftHolder): JsonObject = gsonFireObject.run {
        fromJson(toJson(holder), JsonObject::class.java)
    }
}