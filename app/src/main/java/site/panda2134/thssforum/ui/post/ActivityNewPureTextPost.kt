package site.panda2134.thssforum.ui.post

import android.Manifest
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.PostPureTextBinding
import site.panda2134.thssforum.models.ImageTextPostContent
import site.panda2134.thssforum.models.Location
import site.panda2134.thssforum.models.PostContent
import java.math.BigDecimal
import java.time.Instant


class ActivityNewPureTextPost : ActivityNewPost() {
    private lateinit var binding: PostPureTextBinding
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PostPureTextBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationInit()

        val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
        val draftTitle = pref.getString(getString(R.string.PREF_KEY_PURE_TEXT_TITLE), "")
        val draftContent = pref.getString(getString(R.string.PREF_KEY_PURE_TEXT_CONTENT), "")
        binding.title.setText(draftTitle)
        binding.content.setText(draftContent)
    }

    override fun finish() {
        super.finish()
        val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
        with(pref.edit()) {
            this.putString(getString(R.string.PREF_KEY_PURE_TEXT_TITLE), binding.title.text.toString())
            this.putString(getString(R.string.PREF_KEY_PURE_TEXT_CONTENT), binding.content.text.toString())
            apply()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.send_menu_item -> {
                if (binding.title.text.toString() == "") {
                    noTitleDialog.show()
                }
                else if (binding.content.text.toString() == "") {
                    noContentDialog.show()
                }
                else {
                    val apiService = APIWrapper(this)
                    val imageTextPostContent = ImageTextPostContent(
                        text = binding.content.text.toString(),
                        arrayListOf<String>(),
                        title = binding.title.text.toString()
                    )
                    val postContent = PostContent.makeImageTextPost(
                        imageTextPostContent,
                        location = location,
                        createdAt = Instant.now()
                    )
                    this.lifecycleScope.launch(Dispatchers.IO) {
                        apiService.newPost(postContent)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ActivityNewPureTextPost,
                                R.string.post_success,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    binding.title.text?.clear()
                    binding.content.text?.clear()
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}