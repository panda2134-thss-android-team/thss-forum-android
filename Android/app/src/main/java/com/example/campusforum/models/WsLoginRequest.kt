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
 * @param token 
 * @param uniqueId 可以参考 https://devnote.pro/posts/10000002731155 获得android_id 作为此字段取值
 */

data class WsLoginRequest (
    @SerializedName("token")
    val token: kotlin.String,
    /* 可以参考 https://devnote.pro/posts/10000002731155 获得android_id 作为此字段取值 */
    @SerializedName("unique_id")
    val uniqueId: kotlin.String
)

