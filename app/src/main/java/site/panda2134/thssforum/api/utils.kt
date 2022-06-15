package site.panda2134.thssforum.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.coroutines.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

suspend fun downloadImage(avatarUrl: String): Bitmap? =
    withContext(Dispatchers.IO) {
        try {
            val avatar = Fuel.get(avatarUrl).await(object : ResponseDeserializable<Bitmap> {
                override fun deserialize(inputStream: InputStream): Bitmap {
                    return BitmapFactory.decodeStream(inputStream)
                }
            })
            return@withContext avatar
        } catch (e: FuelError) {
            e.printStackTrace()
            return@withContext null
        }
    }
