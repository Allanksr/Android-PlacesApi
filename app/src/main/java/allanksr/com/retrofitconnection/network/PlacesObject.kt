package allanksr.com.retrofitconnection.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PlacesObject {
    @SerializedName("open_now")
    @Expose
    var openNow: Boolean? = null

    @SerializedName("location")
    @Expose
    var location: PlacesObjectLocation? = null

    class PlacesObjectLocation {
        @SerializedName("lat")
        @Expose
        var lat: Double? = null

        @SerializedName("lng")
        @Expose
        var lng: Double? = null

    }

}