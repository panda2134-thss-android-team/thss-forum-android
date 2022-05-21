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
package org.openapitools.client.models


import com.squareup.moshi.Json
/**
 * 
 * @param uid 
 * @param nickname 
 * @param email 
 * @param avatar 
 * @param passwordHash 发给客户端的内容不含有此字段
 */

data class User (
    @Json(name = "uid")
    val uid: kotlin.String,
    @Json(name = "nickname")
    val nickname: kotlin.String,
    @Json(name = "email")
    val email: kotlin.String,
    @Json(name = "avatar")
    val avatar: kotlin.String,
    /* 发给客户端的内容不含有此字段 */
    @Json(name = "passwordHash")
    val passwordHash: kotlin.String? = null
)

