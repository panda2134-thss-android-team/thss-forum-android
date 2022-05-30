package site.panda2134.thssforum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.campusforum.R
import site.panda2134.thssforum.models.LoginInfo

class MainActivity : AppCompatActivity() {
    private val baseurl = "https://lab.panda2134.site:20443"
    private var tokenMap = mutableMapOf<Int, String>()
    private var uidMap = mutableMapOf<Int, String>()
    private fun login(cnt: Int) {
        val path = "$baseurl/auth/login"
        val loginInfo = LoginInfo(email = "user$cnt@test.com", password = "password")
        APIService.login(loginInfo) { _, _, result ->
            val (bytes, _) = result
            bytes?.let {
                tokenMap[cnt] = bytes.token
                uidMap[cnt] = bytes.uid
            }
            if (cnt < 20) login(cnt + 1)
            else APIService.getPostComments(tokenMap[20]!!, "114514", limit = 114514) { request, _, _ ->
                println(request)
            }
        }
    }
    private fun getFollowingUsers(cnt: Int) {
        APIService.getFollowingUsers(tokenMap[cnt]!!) { _, _, result ->
            val (bytes, _) = result
            bytes?.let {
                for (user in bytes) {
                    println("user$cnt follows ${user.nickname}")
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        login(1)

    }
}