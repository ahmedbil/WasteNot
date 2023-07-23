package com.example.androidapp

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class NetworkManager private constructor(val addr: String){
    val client = OkHttpClient()

    private fun get(path: String, callback: Callback): Call {
        val request: Request = Request.Builder()
            .url("$addr/$path")
            .build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    fun getHeartbeat() = {
        get("heartbeat", heartBeatCallback)
    }

    private var heartBeatCallback = object: Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("Error doing the Request: ${e.message}")
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val responseStr = response.body.string()
                println("Here is the response: $responseStr")
            } else {
                println("Error: ${response.code}")
            }
        }
    }

    companion object {
        private const val domain = "api.aws.melnyk.dev"
        private val instance: NetworkManager = NetworkManager("https://$domain")

        fun getInstance(): NetworkManager {
            return instance
        }
    }
}
