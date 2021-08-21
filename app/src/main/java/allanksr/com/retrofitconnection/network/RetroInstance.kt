package allanksr.com.retrofitconnection.network

import allanksr.com.retrofitconnection.MyApplication
import android.util.Log
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetroInstance {
    private var logTag = "logTag-RetroInstance"
    private val baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/"
    private fun retrofitSetInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    fun getRetroInstance(): RetroService {
        return retrofitSetInstance().create(RetroService::class.java)
    }

    private fun okHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor()) // used if network off OR on
            .addNetworkInterceptor(networkInterceptor()) // only used when network is on
            .addInterceptor(offlineInterceptor())
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS).build()
    }

    private fun httpLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor {message ->
             Log.d(logTag, "log: http log: $message")
        }
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return httpLoggingInterceptor
    }

    /**
     * This interceptor will be called ONLY if the network is available
     * @return
     */


    private val headerCacheControl = "Cache-Control"
    private val headerPragma = "Pragma"
    private fun networkInterceptor(): Interceptor {
        return Interceptor { chain ->
            val response: Response = chain.proceed(chain.request())
            val cacheControl = CacheControl.Builder()
                .maxAge(60*10, TimeUnit.SECONDS)
                .build()
            Log.d(logTag, "networkInterceptor: CacheControl")
            response.newBuilder()
                .removeHeader(headerPragma)
                .removeHeader(headerCacheControl)
                .header(headerCacheControl, cacheControl.toString())
                .build()


        }
    }


    /**
     * This interceptor will be called both if the network is available and if the network is not available
     * @return
     */
    private lateinit var response: Response
    private fun offlineInterceptor(): Interceptor {
        return Interceptor { chain ->
            Log.d(logTag, "offlineInterceptor")
            var request = chain.request()
            // prevent caching when network is on. For that we use the "networkInterceptor"
            if (!MyApplication.instance.isNetworkConnected()) {
                val cacheControlInstance = CacheControl.Builder()
                    .maxStale(60*10, TimeUnit.SECONDS)
                    .build()
                request = request.newBuilder()
                    .removeHeader(headerPragma)
                    .removeHeader(headerCacheControl)
                    .cacheControl(cacheControlInstance)
                    .build()
                Log.d(logTag, "INTERNET IS NOT CONNECTED")

            }


            chain.proceed(request)

        }
    }
}