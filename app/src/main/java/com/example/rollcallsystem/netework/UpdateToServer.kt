package com.example.rollcallsystem.netework

import android.os.Handler
import android.os.Looper
import com.example.rollcallsystem.data.MemberFormat
import com.example.rollcallsystem.data.Status
import com.example.rollcallsystem.feature.BluetoothManager
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class UpdateToServer() {
    val PERIOD_TIME: Long = 10000
    val handler = Handler(Looper.getMainLooper())
    var mApiService: ApiService? = ApiUtils.apiService

    init {

            handler.post(object : Runnable {
                override fun run() {

                    thread {
                        println("Thread ${Thread.currentThread()}")

                        val upload = Status(BluetoothManager.memberList, getCurrentDateTime().toString("yyyy-MM-dd HH:mm:sss"))
                        val body: RequestBody =
                            Gson().toJson(upload).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                        mApiService!!.updatePost(body)
                        .enqueue(object : Callback<String> {

                            override fun onResponse(
                                call: Call<String>,
                                response: Response<String>
                            ) {

                                println("post submitted to API. " + response.body()!!)

                                if (response.isSuccessful) {
                                    println(
                                        "Update to server successful " + response.body()!!.toString()
                                    )
                                }
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                println("Update to server failed :$t")
                            }
                        })

                        handler.postDelayed(this, PERIOD_TIME)}
                }
            })
    }

    fun stopUpdateToServer() = run { handler.removeCallbacksAndMessages(null) }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

}