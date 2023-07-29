package com.example.androidapp.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

typealias Inventory = @Serializable HashMap<Long,FoodItem>
typealias ShoppingList = @Serializable List<FoodItem>

@Serializable
data class FoodItemList(
    val item_list : List<FoodItem>
) {
    fun toBody() : RequestBody {
        val formBody = JSONObject()

        var objectList = mutableListOf<JSONObject>()
        for (item in item_list) {
            objectList.add(item.toJSON())
        }

        formBody.put("item_list", JSONArray(objectList))

        return formBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }
}

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

        val formBody = toJSON()

        //formBody.put("owners", JSONArray(owners))
        return formBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }

    fun toJSON() : JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("amount", amount)
        obj.put("amount_unit", amount_unit)
        obj.put("expiry_date", expiry_date)
        obj.put("purchase_date", purchase_date)
        obj.put("is_expiry_estimated", is_expiry_estimated)
        obj.put("soft_delete", soft_delete)
        return obj
    }

}