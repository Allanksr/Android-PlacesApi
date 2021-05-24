package allanksr.com.retrofitconnection.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PlacesArray {
    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("rating")
    @Expose
    var rating: Double? = null

    @SerializedName("opening_hours")
    @Expose
    var openingHours: PlacesObject? = null

    @SerializedName("geometry")
    @Expose
    var geometry: PlacesObject? = null

}