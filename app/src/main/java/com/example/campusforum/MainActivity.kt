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
import com.github.kittinunf.fuel.gson.responseObject


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Fuel.get("http://183.172.58.70:4523/mock/934436/profile/following/").responseObject<List<User>> { _, _, result ->
            val (userList, error) = result
            if (userList != null) {
                for (user in userList) {
                    println("nickname : ${user.nickname}, uid: ${user.uid}")
                }
            }
        }
    }
}