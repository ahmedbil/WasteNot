package com.example.androidapp

import android.content.Context
import android.util.Log
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.auth.result.AuthSignOutResult
import com.amplifyframework.auth.result.AuthSignUpResult
import com.amplifyframework.core.Amplify
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import com.example.androidapp.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NetworkManager private constructor(val addr: String, val applicationContext: Context) {
    val client = OkHttpClient()
    var accessToken: String? = null


    ////////////////////////////////////////
    // User Login Session Functions Begin //
    ////////////////////////////////////////
    fun amplifyInit(success: (AuthSession) -> Unit) {
        // Initialize Amplify for authentication purposes.
        Amplify.addPlugin(AWSCognitoAuthPlugin())
        Amplify.configure(applicationContext)

        Amplify.Auth.fetchAuthSession(
            {
                accessToken = (it as AWSCognitoAuthSession).userPoolTokensResult.value?.accessToken
                success(it)
            },
            { error -> Log.e(this::class.simpleName, "Error fetching auth session", error) }
        )
    }

    fun confirmSignUp(username: String, code: String, success : (AuthSignUpResult) -> Unit) {
        Amplify.Auth.confirmSignUp(
            username,
            code,
            success,
            { Log.e(this::class.simpleName, "Failed to confirm sign up", it) }
        )
    }

    fun register(cb: (String) -> Unit) {
        get("register", createCallback<String>(cb))
    }

    fun signOut(onComplete: (AuthSignOutResult) -> Unit) {
        Amplify.Auth.signOut(onComplete)
    }

    fun signUp(email: String, username: String, password: String, success: (AuthSignUpResult) -> Unit, error: (AuthException) -> Unit) {
        val options = AuthSignUpOptions.builder()
            .userAttribute(AuthUserAttributeKey.email(), email)
            .build()
        Amplify.Auth.signUp(username, password, options, success, error)
    }

    fun signIn(username: String, password: String, success: (AuthSignInResult) -> Unit, error: (AuthException) -> Unit) {
        Amplify.Auth.signIn(username, password, success, error)
    }

    //////////////////////////////////////
    // User Login Session Functions End //
    //////////////////////////////////////

    fun buildRequest(method: String, path: String, body: RequestBody? = null): Request {
        var request = Request.Builder()
            .url("$addr/$path")
            .header("Authorization", "Bearer $accessToken") // Used to authenticate with AWS load balancer.

        if (method.lowercase().contains("post"))
            request = request.post(body!!)

        return request.build()
    }

    private fun post(path: String, body: RequestBody, callback: Callback): Call {
        val request = buildRequest("post", path, body)
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    private fun get(path: String, callback: Callback): Call {
        val request = buildRequest("get", path)
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    private fun delete(path: String, callback: Callback): Call {
        val request = buildRequest("delete", path)
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    fun getHeartbeat() {
        get("heartbeat",
            createCallback<String>(
                {str -> Log.v(this::class.simpleName, "Heartbeat Response: $str")},
                {str -> str}
            )
        )
    }

    fun getInventory(cb: (Inventory)->Unit) {
        get("users/inventory",createCallback<Inventory>(cb))
    }

    fun getFoodItemFromInventory(foodItem: String, cb: (Inventory)->Unit) {
        get("users/inventory/"+foodItem, createCallback<Inventory>(cb))
    }

    fun addFoodItemToInventory(foodItem: FoodItem, cb: (Inventory)->Unit) {
        post("users/inventory/", foodItem.toBody(), createCallback<Inventory>(cb))
    }

    fun deleteItemFromInventory(foodItem: String, cb: (Inventory)->Unit) {
        delete("users/inventory/"+foodItem,createCallback<Inventory>(cb))
    }

    fun getShoppingList(cb: (ShoppingList)->Unit) {
        get("users/shopping-list", createCallback<ShoppingList>(cb))
    }

    //additem to shoppinglist has @get endpoint annotation on backend
    fun addItemToShoppingList(item: String, cb: (ShoppingList)->Unit) {
        get("users/shopping-list/"+item, createCallback<ShoppingList>(cb))
    }

    fun removeItemFromShoppingList(item: String, cb: (ShoppingList)->Unit) {
        delete("users/shopping-list/"+item, createCallback<ShoppingList>(cb))
    }

    fun deleteShoppingList(cb: (ShoppingList)->Unit) {
        delete("users/shopping-list", createCallback<ShoppingList>(cb))
    }

    /*fun getAnalyticsData(cb: (Int)->Unit) {
        get("users/inventory/analytics", createCallback<ShoppingList>(cb
        {intStr -> intStr.toInt()}))
    }*/

    fun searchRecipesByName(req: RecipeRequest, cb: (List<Recipe>) -> Unit) {
        post("recipes", req.toBody(), createCallback(cb))
    }

    private inline fun <reified T>createCallback (noinline callback: (T) -> Unit): Callback {
        return createCallback(callback, deserializer())
    }

    private fun <T>createCallback (callback: (T) -> Unit, deserializer: (String) -> T): Callback {
        return object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(this::class.simpleName, "Error doing the request: ${e.message}")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val respString = response.body.string()
                    Log.v(this::class.simpleName, "Got response: ${respString}")
                    callback(deserializer(respString))
                } else {
                    Log.e(this::class.simpleName, "Error: ${response.code}")
                }
            }
        }
    }

    private inline fun<reified T> serializer(): (T) -> String {
        return {t: T -> Json.encodeToString(t)}
    }

    private inline fun<reified T> deserializer(): (String) -> T {
        return {str:String -> Json.decodeFromString(str)}
    }
    companion object {
        private const val domain = "api.aws.melnyk.dev"
        private var instance: NetworkManager? = null

        fun initialize(applicationContext: Context, success: (AuthSession) -> Unit): NetworkManager {
            if (instance == null) {
                instance = NetworkManager("https://$domain", applicationContext)
                instance!!.amplifyInit(success)
            }
            return instance!!
        }

        fun getInstance() = instance!!
    }
}
