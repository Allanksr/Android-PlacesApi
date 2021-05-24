package allanksr.com.retrofitconnection.network

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

//rankby=distance
interface RetroService {
    @GET("json")
    fun getPlacesFromApi(
        @Query("location") location: String?,
        @Query("rankby") rankby: String?,
        @Query("type") type: String?,
        @Query("key") key: String?
    ): Observable<PlacesModel>

    @GET("json")
    fun getPlacesNextPageFromApi(
        @Query("pagetoken") pagetoken: String?,
        @Query("key") key: String?
    ): Observable<PlacesModel>
}