package example.findplaces.preferences

import android.content.Context
import android.content.SharedPreferences


class PreferenceProvider(context: Context){
    private val appContext = context
    private val preference: SharedPreferences
    get() = appContext.getSharedPreferences("ID", Context.MODE_PRIVATE)
    /////////////////// insert data in SharedPreferences *******************************************
    fun setString(key: String, word: String){
        preference.edit().putString(key, word).apply()
    }

    /////////////////// return data from SharedPreferences *******************************************
    fun getString(key: String): String{
       return preference.getString(key, "")!!
    }
}