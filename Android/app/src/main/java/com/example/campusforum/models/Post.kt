/**
* THSSForum
* No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
*
* The version of the OpenAPI document: 1.0.0
* 
*
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package com.example.campusforum.models

import com.example.campusforum.models.ImageTextPostContent
import com.example.campusforum.models.Location
import com.example.campusforum.models.MediaPostContent

import com.google.gson.annotations.SerializedName
/**
 * 
 * @param type 
 * @param location 
 * @param id 字符串，新建动态时不发
 * @param by 字符串，新建时不发
 * @param imageTextContent 
 * @param mediaContent 
 */

data class Post (
    @SerializedName("type")
    val type: Post.Type,
    @SerializedName("location")
    val location: Location,
    /* 字符串，新建动态时不发 */
    @SerializedName("id")
    val id: kotlin.String? = null,
    /* 字符串，新建时不发 */
    @SerializedName("by")
    val by: kotlin.String? = null,
    @SerializedName("imageTextContent")
    val imageTextContent: ImageTextPostContent? = null,
    @SerializedName("mediaContent")
    val mediaContent: MediaPostContent? = null
) {

    /**
    * 
    * Values: normal,audio,video
    */
    
    enum class Type(val value: kotlin.String){
        @SerializedName(value="normal")  normal("normal"),
        @SerializedName(value="audio")  audio("audio"),
        @SerializedName(value="video")  video("video");
    }
}

