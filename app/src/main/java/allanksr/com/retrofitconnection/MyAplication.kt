package allanksr.com.retrofitconnection

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.multidex.MultiDex
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application(){
    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(base)
        fix()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

    private fun fix() {
        try {
            val clazz = Class.forName("java.lang.Daemons\$FinalizerWatchdogDaemon")

            val method = clazz.superclass!!.getDeclaredMethod("stop")
            method.isAccessible = true

            val field = clazz.getDeclaredField("INSTANCE")
            field.isAccessible = true

            method.invoke(field.get(null))

        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }
}