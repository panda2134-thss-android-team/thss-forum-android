package site.panda2134.thssforum.ui.post

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerPreviewActivity
import cn.bingoogolapple.photopicker.widget.BGASortableNinePhotoLayout
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.api.gsonFireObject
import site.panda2134.thssforum.databinding.PostTextPicMixBinding
import site.panda2134.thssforum.models.ImageTextPostContent
import site.panda2134.thssforum.models.Location
import site.panda2134.thssforum.models.PostContent
import java.io.File
import java.math.BigDecimal
import java.time.Instant


class ActivityNewTextPicMixPost : ActivityNewPostWithDraft<ActivityNewTextPicMixPost.TextPicMixDraftHolder>(), BGASortableNinePhotoLayout.Delegate {

    data class TextPicMixDraftHolder(
        override val title: String,
        val content: String,
        val images: Array<String>
    ): ActivityNewPostWithDraft.DraftHolder() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TextPicMixDraftHolder

            if (title != other.title) return false
            if (content != other.content) return false
            if (!images.contentEquals(other.images)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = title.hashCode()
            result = 31 * result + content.hashCode()
            result = 31 * result + images.contentHashCode()
            return result
        }

        override fun getActivityClassName(): String = ActivityNewTextPicMixPost::class.java.name
    }

    private lateinit var binding: PostTextPicMixBinding
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
    private fun locationInit() {
        binding.location.visibility = View.GONE
        binding.addLocation.isEnabled = false

        requestLocationPermissions.launch(permissions.toTypedArray())

        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)

        locationClient = AMapLocationClient(applicationContext)
        locationClient.setLocationListener {
            binding.location.visibility = View.VISIBLE
            binding.location.text = it.address
            location = Location(it.address, BigDecimal(it.longitude), BigDecimal(it.latitude))
        }
        locationClient.setLocationOption(AMapLocationClientOption()
            .apply {
                locationPurpose = AMapLocationClientOption.AMapLocationPurpose.Transport
            })

        binding.addLocation.setOnClickListener {
            locationClient.stopLocation()
            locationClient.startLocation()
        }
    }

    private lateinit var api: APIWrapper
    private val tag = "newTextPicMixPost"
    val progressPercentage = MutableLiveData<Int>(0)
    val uploading = MutableLiveData(false)
    private val noPictureDialog: AlertDialog
        get() = AlertDialog.Builder(this)
            .setTitle(getString(R.string.no_photo))
            .setMessage(R.string.please_shot_or_choose_a_photo)
            .create()
    private val noTitleDialog: AlertDialog
        get() = AlertDialog.Builder(this)
            .setTitle(getString(R.string.no_title))
            .setMessage(R.string.please_add_a_title)
            .create()
    private val noContentDialog: AlertDialog
        get() = AlertDialog.Builder(this)
            .setTitle(getString(R.string.no_content))
            .setMessage(R.string.please_add_content)
            .create()

    private val chooseMorePhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != RESULT_OK) return@registerForActivityResult
        binding.photoPicker.addMoreData(BGAPhotoPickerActivity.getSelectedPhotos(it.data))
    }

    private val requestPermissionsResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.values.contains(false)) return@registerForActivityResult
        val takePhotoDir =
            File(Environment.getExternalStorageDirectory(), "THSSForum")

        val photoPickerIntent = BGAPhotoPickerActivity.IntentBuilder(this)
            .cameraFileDir(takePhotoDir)
            .maxChooseCount(binding.photoPicker.maxItemCount - binding.photoPicker.itemCount)
            .selectedPhotos(null) // 当前已选中的图片路径集合
            .pauseOnScroll(false) // 滚动列表时是否暂停加载图片
            .build()
        chooseMorePhoto.launch(photoPickerIntent)
    }

    private val previewPhotoResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        binding.photoPicker.data = BGAPhotoPickerActivity.getSelectedPhotos(it.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = APIWrapper(this)
        binding = PostTextPicMixBinding.inflate(layoutInflater)
        binding.p = this
        setContentView(binding.root)
        locationInit()
        binding.photoPicker.apply {
            maxItemCount = 9
            isSortable = true
            isPlusEnable = true
            isEditable = true

            setDelegate(this@ActivityNewTextPicMixPost)
        }
        draftHolder?.let { holder ->
            binding.title.setText(holder.title)
            binding.content.setText(holder.content)
            binding.photoPicker.addMoreData(holder.images.toCollection(ArrayList()))
        }
    }

    override fun saveDraft() {
        draftHolder = TextPicMixDraftHolder(
            binding.title.text.toString(),
            binding.content.text.toString(),
            binding.photoPicker.data.toTypedArray()
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.send_menu_item -> {
                Log.d(tag, "item clicked")
                if(binding.title.text.toString() == "") {
                    noTitleDialog.show()
                }
                else if(binding.content.text.toString() == "") {
                    noContentDialog.show()
                }
                else if(binding.photoPicker.data.size == 0) {
                    noPictureDialog.show()
                }
                else {
                    lifecycleScope.launch {
                        // 首先上传
                        val images = binding.photoPicker.data
                        val uploadedSizeList = MutableList(images.size) { 0L }
                        val totalSizeList = MutableList(images.size) { 0L }
                        uploading.value = true
                        val uploadedImagePath = images.mapIndexed { index, localImagePath ->
                            lifecycleScope.async(Dispatchers.IO) {
                                api.uploadFileToOSS(Uri.fromFile(File(localImagePath))) { _, uploaded, total ->
                                    uploadedSizeList[index] = uploaded
                                    totalSizeList[index] = total
                                    progressPercentage.postValue((uploadedSizeList.sum() * 100 / totalSizeList.sum()).toInt())
                                }
                            }
                        }.awaitAll()
                        uploading.value = false
                        val postContent =
                            PostContent.makeImageTextPost(
                                ImageTextPostContent(
                                    title=binding.title.text.toString(),
                                    text=binding.content.text.toString(),
                                    images=uploadedImagePath.map { it.toString() }.toCollection(ArrayList())
                                ),
                                createdAt = Instant.now()
                            )
                        withContext(Dispatchers.IO) {
                            api.newPost(postContent)
                        }
                        binding.title.text?.clear()
                        binding.content.text?.clear()
                        binding.photoPicker.data.clear()
                        Toast.makeText(this@ActivityNewTextPicMixPost, R.string.post_success, Toast.LENGTH_SHORT).show()
                        isPostSent = true
                        finish()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClickAddNinePhotoItem(
        sortableNinePhotoLayout: BGASortableNinePhotoLayout?,
        view: View?,
        position: Int,
        models: ArrayList<String>?
    ) {
        requestPermissionsResult.launch(listOf(Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE).toTypedArray())
    }

    override fun onClickDeleteNinePhotoItem(
        sortableNinePhotoLayout: BGASortableNinePhotoLayout?,
        view: View?,
        position: Int,
        model: String?,
        models: ArrayList<String>?
    ) {
        binding.photoPicker.removeItem(position)
    }

    override fun onClickNinePhotoItem(
        sortableNinePhotoLayout: BGASortableNinePhotoLayout?,
        view: View?,
        position: Int,
        model: String?,
        models: ArrayList<String>?
    ) {
        val photoPickerPreviewIntent = BGAPhotoPickerPreviewActivity.IntentBuilder(this)
            .previewPhotos(models)
            .selectedPhotos(models)
            .maxChooseCount(binding.photoPicker.maxItemCount)
            .currentPosition(position)
            .isFromTakePhoto(false)
            .build()
        previewPhotoResult.launch(photoPickerPreviewIntent)
    }

    override fun onNinePhotoItemExchanged(
        sortableNinePhotoLayout: BGASortableNinePhotoLayout?,
        fromPosition: Int,
        toPosition: Int,
        models: ArrayList<String>?
    ) {
    }

    override fun deserializeDraftJson(j: JsonObject): TextPicMixDraftHolder = gsonFireObject.fromJson(j, TextPicMixDraftHolder::class.java)
    override fun serializeDraftJson(holder: TextPicMixDraftHolder): JsonObject = gsonFireObject.run {
        fromJson(toJson(holder), JsonObject::class.java)
    }
}