package example.findplaces.viewModels

import android.content.Context
import android.util.Log
import example.findplaces.BuildConfig
import example.findplaces.constants.Const
import example.findplaces.preferences.PreferenceProvider
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class NearbyPlaceRepository(context: Context) {
    private var logTag = "logTag-MapsRepository"
    private var myContext = context
    private lateinit var connection: HttpURLConnection
    private var reader: BufferedReader? = null
    private lateinit var preferenceProvider: PreferenceProvider
    private lateinit var jsonObjectToSend : JSONObject
    private val apiKey = BuildConfig.API_KEY // -< define API_KEY in build_gradle Module
    fun getNearbyPlacesData(latitude: Double, longitude: Double, proximityRadius: Int, type: String): JSONObject{
        preferenceProvider = PreferenceProvider(myContext)
        val googlePlacesEndPoint = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?")
        googlePlacesEndPoint.append("location=$latitude,$longitude")
        googlePlacesEndPoint.append("&radius=$proximityRadius")
        googlePlacesEndPoint.append("&type=$type")
        googlePlacesEndPoint.append("&key=$apiKey")
        Log.d(logTag, "getNearbyPlacesData: $googlePlacesEndPoint")

        try {
            val url = URL("$googlePlacesEndPoint")
            connection = (url.openConnection() as HttpURLConnection?)!!
            connection.connect()
            connection.readTimeout = 5000
            val inObject = BufferedReader(InputStreamReader(connection.inputStream))
            var inputLineObject: String?
            val dataObject = StringBuffer()
            while (inObject.readLine().also { inputLineObject = it } != null) {
                dataObject.append(inputLineObject)
            }
            inObject.close()

            val jsonData: String = dataObject.toString()
            val objectData = JSONTokener(jsonData).nextValue() as JSONObject
            jsonObjectToSend = JSONObject()
            if(objectData.has("error_message")){
                val array = JSONArray()
                val lat = "0"
                val lng = "0"
                array.put(lat)
                array.put(lng)
                array.put(objectData.getString("error_message"))
                array.put("undefined")
                array.put("undefined")
                jsonObjectToSend.put("singleValue", array)
            }else{
                if(objectData.has("next_page_token")){
                    preferenceProvider.setString(
                        Const.pageToken,
                        objectData.getString("next_page_token")
                    )
                    preferenceProvider.setString(Const.placeType, type)
                }else{
                    preferenceProvider.setString(Const.pageToken, "")
                }

                if(objectData.getString("results").length > 2){
                    val itemsComments = objectData.getString("results")
                    val jsonItems =  JSONArray(itemsComments)
                    if(jsonItems.length()>1){
                        for (i in 0 until jsonItems.length()) {
                            val jsonObject = jsonItems.getJSONObject(i)
                            val geometry: JSONObject = jsonObject.getJSONObject("geometry")
                            val location: JSONObject = geometry.getJSONObject("location")
                            val lat = location.getString("lat")
                            val lng = location.getString("lng")
                            jsonObjectToSend.accumulate("lat", lat)
                            jsonObjectToSend.accumulate("lng", lng)
                            jsonObjectToSend.accumulate("name", jsonObject.getString("name"))
                            if(jsonObject.has("opening_hours")){
                                val openNow: JSONObject = jsonObject.getJSONObject("opening_hours")
                                jsonObjectToSend.accumulate("open_now", openNow.getString("open_now"))
                            }else{
                                jsonObjectToSend.accumulate("open_now", "undefined")
                            }
                            if(jsonObject.has("rating")){
                                jsonObjectToSend.accumulate("rating", jsonObject.getString("rating"))
                            }else{
                                jsonObjectToSend.accumulate("rating", "undefined")
                            }
                        }
                    }else{
                        val array = JSONArray()
                        val jsonObject = jsonItems.getJSONObject(0)
                        val geometry: JSONObject = jsonObject.getJSONObject("geometry")
                        val location: JSONObject = geometry.getJSONObject("location")
                        val lat = location.getString("lat")
                        val lng = location.getString("lng")
                        array.put(lat)
                        array.put(lng)
                        array.put(jsonObject.getString("name"))
                        if(jsonObject.has("opening_hours")){
                            val openNow: JSONObject = jsonObject.getJSONObject("opening_hours")
                            array.put(openNow.getString("open_now"))
                        }else{
                            array.put("undefined")
                        }
                        if(jsonObject.has("rating")){
                            array.put(jsonObject.getString("rating"))
                        }else{
                            array.put("undefined")
                        }
                        jsonObjectToSend.put("singleValue", array)
                    }

                }else{
                    Log.d(logTag, "no places to show: ")
                    val array = JSONArray()
                    val lat = "0"
                    val lng = "0"
                    array.put(lat)
                    array.put(lng)
                    array.put("increase the distance radius")
                    array.put("undefined")
                    array.put("undefined")
                    jsonObjectToSend.put("singleValue", array)
                }
            }


        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }catch (e: IOException) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
            try {
                if (reader != null) {
                    reader?.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return jsonObjectToSend
    }

    private lateinit var jsonObjectToAppend : JSONObject
    fun getMoreNearbyPlacesData(pageToken: String): JSONObject{
        preferenceProvider = PreferenceProvider(myContext)
        val googlePlacesEndPoint = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?")
        googlePlacesEndPoint.append("pagetoken=$pageToken")
        googlePlacesEndPoint.append("&key=$apiKey")
        Log.d(logTag, "getMoreNearbyPlacesData: $googlePlacesEndPoint")

        try {
            val url = URL("$googlePlacesEndPoint")
            connection = (url.openConnection() as HttpURLConnection?)!!
            connection.connect()
            connection.readTimeout = 5000
            val inObject = BufferedReader(InputStreamReader(connection.inputStream))
            var inputLineObject: String?
            val dataObject = StringBuffer()

            while (inObject.readLine().also { inputLineObject = it } != null) {
                dataObject.append(inputLineObject)
            }
            inObject.close()

            val jsonData: String = dataObject.toString()
            val objectData = JSONTokener(jsonData).nextValue() as JSONObject

            if(objectData.has("next_page_token")){
                preferenceProvider.setString(
                    Const.pageToken,
                    objectData.getString("next_page_token")
                )
            }else{
                preferenceProvider.setString(Const.pageToken, "")
            }
            if(objectData.getString("results").length > 2){
                val itemsComments = objectData.getString("results")
                val jsonItems =  JSONArray(itemsComments)
                jsonObjectToAppend = JSONObject()
                for (i in 0 until jsonItems.length()) {
                    val jsonObject = jsonItems.getJSONObject(i)
                    val geometry: JSONObject = jsonObject.getJSONObject("geometry")
                    val location: JSONObject = geometry.getJSONObject("location")
                    val lat = location.getString("lat")
                    val lng = location.getString("lng")
                    jsonObjectToAppend.accumulate("lat", lat)
                    jsonObjectToAppend.accumulate("lng", lng)
                    jsonObjectToAppend.accumulate("name", jsonObject.getString("name"))
                    if(jsonObject.has("opening_hours")){
                        val openNow: JSONObject = jsonObject.getJSONObject("opening_hours")
                        jsonObjectToAppend.accumulate("open_now", openNow.getString("open_now"))
                    }else{
                        jsonObjectToAppend.accumulate("open_now", "undefined")
                    }
                    if(jsonObject.has("rating")){
                        jsonObjectToAppend.accumulate("rating", jsonObject.getString("rating"))
                    }else{
                        jsonObjectToAppend.accumulate("rating", "undefined")
                    }
                }
            }else{
                Log.d(logTag, "no places to show: ")
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }catch (e: IOException) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
            try {
                if (reader != null) {
                    reader?.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return jsonObjectToAppend
    }

}
