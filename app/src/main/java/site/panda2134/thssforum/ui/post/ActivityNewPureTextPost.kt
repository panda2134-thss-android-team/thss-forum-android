package site.panda2134.thssforum.ui.post

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Visibility
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.navigation.ActivityNavigator
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.PostPureTextBinding

class ActivityNewPureTextPost : ActivityNewPost() {
    private lateinit var binding: PostPureTextBinding
    lateinit var locationClient: AMapLocationClient

    private val permissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE)

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            for (perm in permissions) {
                if (it[perm] != true) {
                    binding.addLocation.isEnabled = false
                    return@registerForActivityResult
                }
            }
            binding.addLocation.isEnabled = true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PostPureTextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.location.visibility = View.GONE
        binding.addLocation.isEnabled = false

        requestMultiplePermissions.launch(permissions.toTypedArray())

        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)

        locationClient = AMapLocationClient(applicationContext)
        locationClient.setLocationListener {
            Log.d("AMAP", it.address)
            binding.addLocation.visibility = View.GONE
            binding.location.visibility = View.VISIBLE
            binding.location.text = it.address
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
}