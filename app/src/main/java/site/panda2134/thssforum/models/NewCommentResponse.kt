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
 * @param commentId 
 * @param postId 
 */

data class NewCommentResponse (
    @SerializedName("comment_id")
    val commentId: String,
    @SerializedName("post_id")
    val postId: String
)

