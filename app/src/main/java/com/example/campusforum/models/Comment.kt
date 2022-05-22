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


import com.google.gson.annotations.SerializedName
/**
 * 
 * @param content 
 * @param parentCommentId 若非空则说明此评论是回复另一个评论，此字段为回复的评论id
 */

data class Comment (
    @SerializedName("content")
    val content: kotlin.String,
    /* 若非空则说明此评论是回复另一个评论，此字段为回复的评论id */
    @SerializedName("parentCommentId")
    val parentCommentId: kotlin.String? = null
)
