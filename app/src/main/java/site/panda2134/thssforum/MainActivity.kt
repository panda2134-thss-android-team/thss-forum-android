package site.panda2134.thssforum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.campusforum.R
import com.example.campusforum.databinding.ActivityMainBinding
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import kotlinx.coroutines.*
import site.panda2134.thssforum.models.LoginRequest
import java.lang.Exception

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

        api = APIService(this)
        binding.title.setOnClickListener {
            mainScope.launch(Dispatchers.IO) {
                Log.d("start", "started corot")
                for (cnt in 1..3) {
                    Log.d("work", "$cnt")
                    val loginInfo = LoginRequest(email = "user$cnt@test.com", password = "pass")
                    try {
                        val loginResponse = api.login(loginInfo)
                        tokenMap[cnt] = loginResponse.token
                        uidMap[cnt] = loginResponse.uid
                        Log.d("token", "$cnt     ${loginResponse.token}")
                    } catch (e: Throwable) {
                        Log.d("error", e.stackTraceToString())
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}