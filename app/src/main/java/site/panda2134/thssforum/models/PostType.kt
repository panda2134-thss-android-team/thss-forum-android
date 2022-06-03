package site.panda2134.thssforum.models

import com.google.gson.annotations.SerializedName

enum class PostType(val value: String){
    @SerializedName(value="normal")  normal("normal"),
    @SerializedName(value="audio")  audio("audio"),
    @SerializedName(value="video")  video("video");
}