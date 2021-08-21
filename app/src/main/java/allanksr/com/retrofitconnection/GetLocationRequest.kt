package allanksr.com.retrofitconnection

import com.google.android.gms.location.LocationRequest



class GetLocationRequest {
    private var logTag = "logTag-GetLocationRequest"
    private lateinit var locationRequest: LocationRequest

     fun createLocationRequest():LocationRequest {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

}