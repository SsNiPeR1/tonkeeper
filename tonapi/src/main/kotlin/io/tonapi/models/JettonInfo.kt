/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.tonapi.models

import io.tonapi.models.AccountAddress
import io.tonapi.models.JettonMetadata
import io.tonapi.models.JettonVerificationType

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param mintable 
 * @param totalSupply 
 * @param metadata 
 * @param verification 
 * @param holdersCount 
 * @param admin 
 */


data class JettonInfo (

    @Json(name = "mintable")
    val mintable: kotlin.Boolean,

    @Json(name = "total_supply")
    val totalSupply: kotlin.String,

    @Json(name = "metadata")
    val metadata: JettonMetadata,

    @Json(name = "verification")
    val verification: JettonVerificationType,

    @Json(name = "holders_count")
    val holdersCount: kotlin.Int,

    @Json(name = "admin")
    val admin: AccountAddress? = null

)

