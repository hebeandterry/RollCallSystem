package com.example.rollcallsystem.network

import android.os.Handler
import android.os.Looper
import com.example.rollcallsystem.feature.BluetoothManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
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

                        val body: RequestBody =
                            createUpdateJson().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                        mApiService!!.updatePost(body)
                        .enqueue(object : Callback<String> {

                            override fun onResponse(
                                call: Call<String>,
                                response: Response<String>
                            ) {

                                //println("post submitted to API. " + response.body()!!)

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

    fun createUpdateJson(): String{
        val jsonObject1 = JSONObject()
        val jsonObject2 = JSONObject()
        val jsonArray = JSONArray()
        for (member in BluetoothManager.memberList)
        {
            if (member.bArrived)
            {
                val data = JSONObject()
                data.put("mac", member.mac)
                data.put("position_id", member.position_id)

                jsonArray.put(data)
            }
        }
        jsonObject1.put("beacons", jsonArray)
        jsonObject1.put("locate_time", getCurrentDateTime().toString("yyyy-MM-dd HH:mm:sss"))

        jsonObject2.put("data", jsonObject1.toString())

        return jsonObject2.toString();
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