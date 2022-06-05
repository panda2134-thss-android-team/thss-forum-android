package site.panda2134.thssforum

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIService
import site.panda2134.thssforum.databinding.ActivityMainBinding
import site.panda2134.thssforum.models.LoginRequest

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_post, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        val apiService = APIService(this)
        Log.d("MainActivity", "111")
        this.lifecycleScope.launch(Dispatchers.IO) {
            val loginResponse = apiService.login(LoginRequest(email = "user1@test.com", password = "password"))
            Log.d("MainActivity", "token = ${loginResponse.token}, uid = ${loginResponse.uid}")
        }
        Log.d("MainActivity", "222")
    }
}
