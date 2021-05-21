package example.findplaces.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class NearbyPlaceViewModel private constructor(private val nearbyPlaceRepository: NearbyPlaceRepository) : ViewModel() {
     val googlePlacesData = MutableLiveData<JSONObject>()
     val googleMorePlacesData = MutableLiveData<JSONObject>()

    fun getPlaces(latitude: Double, longitude: Double, proximityRadius: Int, type: String){
        CoroutineScope(Dispatchers.Main).launch {
            val data =  withContext(Dispatchers.Default){
                nearbyPlaceRepository.getNearbyPlacesData(latitude, longitude, proximityRadius, type)
            }
            googlePlacesData.value = data
        }
    }

    fun getMorePlaces(pageToken: String){
        CoroutineScope(Dispatchers.Main).launch {
            val data =  withContext(Dispatchers.Default){
                nearbyPlaceRepository.getMoreNearbyPlacesData(pageToken)
            }
            googleMorePlacesData.value = data
        }
    }

    @Suppress("UNCHECKED_CAST")
    class MapsViewModelFactory(private val nearbyPlaceRepository: NearbyPlaceRepository): ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return NearbyPlaceViewModel(nearbyPlaceRepository) as T
        }
    }
}