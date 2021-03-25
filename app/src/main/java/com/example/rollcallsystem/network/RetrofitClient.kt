package com.example.rollcallsystem.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

val trustAllCerts = arrayOf<TrustManager>(
    object : X509TrustManager {
        override fun checkClientTrusted(
            chain: Array<X509Certificate?>?,
            authType: String?
        ) {
        }

        override fun checkServerTrusted(
            chain: Array<X509Certificate?>?,
            authType: String?
        ) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf<X509Certificate>()
        }
    }
)

object RetrofitClient {

    var retrofit: Retrofit? = null

    // Create retrofit client object setting.
    fun getClient(baseUrl: String): Retrofit? {
        if (retrofit == null) {

            retrofit = Retrofit.Builder()
                //.client(client)
                .client(getUnsafeOkHttpClient())
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofit

    }

    private fun getUnsafeOkHttpClient(): OkHttpClient? {
        return try {
            //TODO While release in Google Play Change the Level to NONE
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            OkHttpClient.Builder()
                .sslSocketFactory(getSSLSocketFactory(), (trustAllCerts[0] as X509TrustManager))
                .hostnameVerifier(HostnameVerifier { _: String?, _: SSLSession? -> true })
                .addInterceptor(interceptor)
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun getSSLSocketFactory() : SSLSocketFactory {
        return try {
            val sslContext: SSLContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null, trustAllCerts, SecureRandom())

            sslContext.socketFactory
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
