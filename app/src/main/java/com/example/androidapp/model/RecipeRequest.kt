package com.example.androidapp.model

import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray

@Serializable
data class RecipeRequest (
    val query: String,
    val pageSize: Int,
    val includedIngredients: List<String>,
    val excludedIngredients: List<String>
){
    fun toBody(): RequestBody {
        val formBody = JSONObject()
        formBody.put("name", "")
        formBody.put("page_size", pageSize)
        formBody.put("includedIngredients", JSONArray())
        formBody.put("excludedIngredients", JSONArray())
        return formBody.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }
}
