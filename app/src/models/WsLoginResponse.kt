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
 * @param type 
 */

data class WsLoginResponse (
    @Json(name = "type")
    val type: WsLoginResponse.Type
) {

    /**
    * 
    * Values: success,error
    */
    
    enum class Type(val value: kotlin.String){
        @Json(name = "success") success("success"),
        @Json(name = "error") error("error");
    }
}
