package allanksr.com.retrofitconnection

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex


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