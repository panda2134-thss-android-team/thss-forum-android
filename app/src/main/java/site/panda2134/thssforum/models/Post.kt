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
package site.panda2134.thssforum.models

import com.google.gson.annotations.SerializedName


/**
 * 
 * @param type 
 * @param id
 * @param by
 * @param location 
 * @param imageTextContent 
 * @param mediaContent 
 */
data class PostResponse (
    @SerializedName("type")
    val type: PostType,
    @SerializedName("id")
    val id: String,
    @SerializedName("by")
    val by: String,
    @SerializedName("location")
    val location: Location? = null,
    @SerializedName("imageTextContent")
    val imageTextContent: ImageTextPostContent? = null,
    @SerializedName("mediaContent")
    val mediaContent: MediaPostContent? = null
)

data class Post (
    val author: User,
    val postContent: PostContent
)

class PostContent private constructor (
    @SerializedName("type")
    val type: PostType,
    @SerializedName("location")
    val location: Location? = null,
    @SerializedName("imageTextContent")
    val imageTextContent: ImageTextPostContent? = null,
    @SerializedName("mediaContent")
    val mediaContent: MediaPostContent? = null,
    @Transient
    val id: String? = null
)  {
    companion object {
        fun makeImageTextPost (content: ImageTextPostContent, location: Location? = null) =
            PostContent(PostType.normal, location, content, null)
        fun makeAudioPost (content: MediaPostContent, location: Location? = null) =
            PostContent(PostType.audio, location, null, content)
        fun makeVideoPost (content: MediaPostContent, location: Location? = null) =
            PostContent(PostType.video, location, null, content)
        fun fromPostResponse (response: PostResponse) =
            PostContent(response.type, response.location, response.imageTextContent, response.mediaContent, response.id)
    }
}

