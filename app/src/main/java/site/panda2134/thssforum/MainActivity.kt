package site.panda2134.thssforum

import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.example.campusforum.R
import com.example.campusforum.databinding.ActivityMainBinding
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.google.gson.Gson
import kotlinx.coroutines.*
import site.panda2134.thssforum.models.LoginRequest
import java.lang.Exception
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    private val baseurl = "https://lab.panda2134.site:20443"
    private var tokenMap = mutableMapOf<Int, String>()
    private var uidMap = mutableMapOf<Int, String>()

    private val mainScope = MainScope()
    private lateinit var binding: ActivityMainBinding
    private lateinit var api: APIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val perm = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)

        if (perm != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                listOf(android.Manifest.permission.READ_EXTERNAL_STORAGE).toTypedArray(),
                1
            )
        }

        api = APIService(this)
        binding.login.setOnClickListener {
            mainScope.launch(Dispatchers.IO) {
                val loginInfo = LoginRequest(email = "user1@test.com", password = "password")
                try {
                    val loginResponse = api.login(loginInfo)
                    Log.d("token", loginResponse.token)

                } catch (e: FuelError) {
                    Log.d("error", e.response.statusCode.toString())
                }
            }
        }
        binding.debug.setOnClickListener {
            mainScope.launch(Dispatchers.IO) {
                try {
                    api.uploadFileToOSS(
                        Uri.parse("file:/sdcard/Pictures/Screenshots/Screenshot_20220603-151946_THSSForum.png")
                    ) { _, currentSize, totalSize ->
                        binding.progressBar.min = 0
                        binding.progressBar.max = 100
                        binding.progressBar.progress = (currentSize * 100 / totalSize).toInt()
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}