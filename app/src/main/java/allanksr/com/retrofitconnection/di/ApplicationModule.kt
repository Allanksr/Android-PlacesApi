package allanksr.com.retrofitconnection.di

import allanksr.com.retrofitconnection.BlinkText
import allanksr.com.retrofitconnection.GetLocationRequest
import allanksr.com.retrofitconnection.network.RetroInstance
import allanksr.com.retrofitconnection.network.RetroService
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent ::class)
object ApplicationModule {

    @Singleton
    @Provides
    fun initLocationRequest() = GetLocationRequest().createLocationRequest()

    @Singleton
    @Provides
    fun initBlink(@ApplicationContext appContext: Context): BlinkText {
        return BlinkText(appContext)
    }

    @Singleton
    @Provides
    fun initRetrofit() = RetroInstance().getRetroInstance()

}