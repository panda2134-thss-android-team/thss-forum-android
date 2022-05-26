package com.example.campusforum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.example.campusforum.models.User

class MainActivity : AppCompatActivity() {
    private fun <T> jsonToList(jsonList: String): List<T> {
        val gson = Gson()
        return gson.fromJson(jsonList, object : TypeToken<ArrayList<T>>() {}.type)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val httpAsync = "http://183.172.58.70:4523/mock/934436/profile/following/"
            .httpGet()
            .responseString { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                    }
                    is Result.Success -> {
                        val data = result.get()
                        val usersList = jsonToList<User>(data)
                        println(data)
                        println(usersList.first().nickname)
                    }
                }
            }

        httpAsync.join()
    }
}