package com.example.rollcallsystem.network


import com.example.rollcallsystem.data.Beacon
import com.example.rollcallsystem.data.AccountToken
import com.example.rollcallsystem.data.RollCallPosition
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("api/api-token-auth/")
    @Headers("Content-Type:application/json")
    fun loginPost(@Body loginAccount: RequestBody)
              : Call<AccountToken>

    @POST("api/")
    @Headers("Content-Type:application/json")
    fun getBeaconsPost(@Header ("Authorization") auth: String, @Body data: RequestBody)
              : Call<Beacon>

    @POST("api/")
    @Headers("Content-Type:application/json")
    fun getRollCallPositionPost(@Header ("Authorization") auth: String, @Body data: RequestBody)
            : Call<RollCallPosition>

    @POST("api/sentinel-webhook/")
    @Headers("Content-Type:application/json")
    fun updatePost(@Body data: RequestBody): Call<String>

}

object ApiUtils {

    private const val BASE_URL = "https://59.125.177.137:44351/"
    //private const val BASE_URL = "https://ensmlubskvwaq.x.pipedream.net/"

    val apiService: ApiService
        get() = RetrofitClient.getClient(BASE_URL)!!.create(ApiService::class.java)

}
