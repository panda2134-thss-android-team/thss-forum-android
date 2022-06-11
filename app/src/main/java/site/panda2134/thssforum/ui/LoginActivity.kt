package site.panda2134.thssforum.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.github.kittinunf.fuel.core.FuelError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.MainActivity
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIService
import site.panda2134.thssforum.databinding.ActivityLoginBinding
import site.panda2134.thssforum.models.LoginRequest
import site.panda2134.thssforum.models.RegisterRequest

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private val noError: Boolean
        get() = binding.editTextEmail.text.isNotBlank() && binding.editTextPassword.text.isNotBlank()
    lateinit var api: APIService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        api = APIService(this)

        val updateButtonState = {
            binding.loginButton.isEnabled = noError
            binding.registerButton.isEnabled = noError
        }
        binding.editTextEmail.addTextChangedListener { updateButtonState() }
        binding.editTextPassword.addTextChangedListener { updateButtonState() }
        binding.loginButton.setOnClickListener {
            MainScope().launch(Dispatchers.IO) {
                try {
                    api.login(LoginRequest(binding.editTextEmail.text.toString(),
                        binding.editTextPassword.text.toString()))
                    gotoMainActivity()
                } catch (e: FuelError) {
                }
            }
        }

        binding.registerButton.setOnClickListener {
            MainScope().launch(Dispatchers.IO) {
                try {
                    api.register(RegisterRequest("新用户", binding.editTextPassword.text.toString(), binding.editTextEmail.text.toString()))
                    api.login(LoginRequest(binding.editTextEmail.text.toString(), binding.editTextPassword.text.toString()))
                    gotoMainActivity()
                } catch (e: FuelError) {
                    if (e.response.statusCode == 400) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, R.string.TOAST_EMAIL_USED, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private suspend fun gotoMainActivity () {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@LoginActivity, R.string.TOAST_LOGIN_SUCCESS, Toast.LENGTH_LONG).show()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

//    override fun onBackPressed() {
//        return // disable back button
//    }
}