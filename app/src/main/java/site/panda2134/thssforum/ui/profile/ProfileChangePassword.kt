package site.panda2134.thssforum.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.github.kittinunf.fuel.core.FuelError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.ProfileChangePasswordBinding
import site.panda2134.thssforum.models.ChangePasswordRequest

class ProfileChangePassword : ActivityProfileItem() {
    lateinit var binding: ProfileChangePasswordBinding
    private val noError: Boolean
        get() = binding.editTextOldPassword.text.isNotBlank() && binding.editTextNewPassword.text.isNotBlank()
    lateinit var api: APIWrapper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ProfileChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        api = APIWrapper(this)

        val updateButtonState = {
            binding.changePasswordButton.isEnabled = noError
        }
        binding.editTextOldPassword.addTextChangedListener { updateButtonState() }
        binding.editTextNewPassword.addTextChangedListener { updateButtonState() }

        binding.changePasswordButton.setOnClickListener {
            MainScope().launch(Dispatchers.IO) {
                try {
                    api.silentHttpResponseStatus = listOf(403)
                    api.changePassword(ChangePasswordRequest(newPassword = binding.editTextNewPassword.text.toString(), oldPassword = binding.editTextOldPassword.text.toString()))
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileChangePassword, R.string.TOAST_CHANGE_PASSWORD_SUCCESS, Toast.LENGTH_LONG).show()
                    }
                    finish()
                } catch (e: FuelError) {
                    if (e.response.statusCode == 403) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ProfileChangePassword, R.string.TOAST_WRONG_OLD_PASSWORD, Toast.LENGTH_LONG).show()
                        }
                    }
                } finally {
                    api.silentHttpResponseStatus = listOf()
                }
            }
        }
    }


//    override fun onBackPressed() {
//        return // disable back button
//    }
}