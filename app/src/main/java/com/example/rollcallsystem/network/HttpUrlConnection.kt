package com.example.rollcallsystem.network

import com.example.rollcallsystem.ui.HttpRequestType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class HttpUrlConnection {

    private var mApiService: ApiService = ApiUtils.apiService
    private var listener: HttpRequestListener? = null

    //Register a listener form RollCallActivity to get scan result.
    fun registerListener(listener: HttpRequestListener) {
        this.listener = listener
    }

    fun httpRequest (data: String, type: HttpRequestType, vararg parameters: String): Any? {

        try {
            val body: RequestBody =
                data.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val response = when (type) {
                HttpRequestType.LOGIN -> {
                    mApiService.loginPost(body).execute()
                }
                HttpRequestType.GET_ROLL_CALL_POSITION-> { //only pass one parameter
                    mApiService.getRollCallPositionPost(parameters[0], body).execute()
                }
                else -> {
                    mApiService.getBeaconsPost(parameters[0], body).execute()
                }
            }

            return if (response.isSuccessful) {
                println("result ${response.body()}")
                response.body()
            } else {
                this.listener?.onHttpRequestErrorResponse(response.errorBody()?.string())
                null
            }
        }catch (e: IOException)
        {
            this.listener?.onHttpRequestErrorResponse(e.toString())
            println("Login error: $e")
            return null
        }
    }

}

interface HttpRequestListener {
    fun onHttpRequestResponse()

    fun onHttpRequestErrorResponse(errorBody: String?)
}