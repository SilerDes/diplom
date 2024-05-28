package com.kazbekov.invent.network

import android.util.Log
import com.kazbekov.invent.network.interceptors.HeaderInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

object Network {

    private const val BASE_URL = "https://kazbekovandrew.fvds.ru/"
    private const val API_KEY = "11e3b037-1b70-4eb6-b44a-5d3837041621"

    private val okHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addNetworkInterceptor(HeaderInterceptor(mapOf("AUTHKEY" to API_KEY)))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: InventApiService = retrofit.create()

    object Common {
        const val SERVER_ANY_500_OR_INTERNET = 5001

        fun <T : Any?> execute(
            call: Call<T>,
            onSuccessful: (response: T?) -> Unit,
            onFailure: (errorCode: Int, message: String) -> Unit
        ) {
            call.enqueue(object : Callback<T?> {
                override fun onResponse(call: Call<T?>, response: Response<T?>) {
                    if (response.isSuccessful) {
                        onSuccessful(response.body())
                    } else {
                        onFailure(
                            response.code(),
                            response.headers()["x-error-message"]?.uppercase()
                                ?: "Что-то пошло не так"
                        )
                    }
                }

                override fun onFailure(call: Call<T?>, t: Throwable) {
                    onFailure(SERVER_ANY_500_OR_INTERNET, "")
                }

            })
        }
    }

    object Errors {
        const val ERROR_400_BAD_REQUEST = "BAD REQUEST"
        const val ERROR_401_UNAUTHORIZED = "UNAUTHORIZED"
        const val ERROR_403_API_KEY = "FORBIDDEN"
        const val ERROR_403_STATUS = "FORBIDDEN BY STATUS"
        const val ERROR_404_NOT_FOUND = "NOT FOUND"
        const val ERROR_404_NOT_FOUND_SELF = "NOT FOUND SELF"
        const val ERROR_409_CONFLICT = "CONFLICT"
        const val ERROR_410_GONE = "GONE"
        const val ERROR_423_LOCKED = "LOCKED"
    }
}