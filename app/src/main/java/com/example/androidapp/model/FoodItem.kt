package com.example.androidapp.model

import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

typealias Inventory = @Serializable HashMap<Long,FoodItem>
typealias ShoppingList = @Serializable List<FoodItem>

@Serializable
data class FoodItem(
    val id: Long,
    val name: String,
    val amount: Double,
    val amount_unit: String,
    val expiry_date: String,
    val purchase_date: String,
    val is_expiry_estimated: Boolean,
    val soft_delete: Boolean,
    val owners: List<String>
) {

    fun toBody(): RequestBody {
        val formBody = JSONObject()
        formBody.put("id", id)
        formBody.put("name", name)
        formBody.put("amount", amount)
        formBody.put("amount_unit", amount_unit)
        formBody.put("expiry_date", expiry_date)
        formBody.put("purchase_date", purchase_date)
        formBody.put("is_expiry_estimated", is_expiry_estimated)
        formBody.put("soft_delete", soft_delete)
        //formBody.put("owners", JSONArray(owners))
        return formBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }

}