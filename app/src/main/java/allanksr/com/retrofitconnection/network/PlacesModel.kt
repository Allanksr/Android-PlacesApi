package allanksr.com.retrofitconnection.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PlacesModel(
        var loading: Boolean = false,

        @SerializedName("next_page_token")
        @Expose
        var nextPageToken: String?,

        @SerializedName("results")
        @Expose
        var resultsArray: ArrayList<PlacesArray>
    )


