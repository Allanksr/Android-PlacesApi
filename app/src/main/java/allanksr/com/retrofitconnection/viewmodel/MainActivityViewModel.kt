package allanksr.com.retrofitconnection.viewmodel

import allanksr.com.retrofitconnection.network.PlacesModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import allanksr.com.retrofitconnection.network.RetroService
import android.util.Log
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import allanksr.com.retrofitconnection.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private var initRetrofit: RetroService) : ViewModel() {
    private var logTag = "logTag-MainActivityViewModel"
    private val apiKey = BuildConfig.API_KEY
    var compositeDisposable: CompositeDisposable? = null
    lateinit var placesModel: PlacesModel
    var places: MutableLiveData<PlacesModel> = MutableLiveData()
    private var placeRadius = 20000
    fun getPlacesObserver(): MutableLiveData<PlacesModel> {
        return places
    }

    fun placesByDistance(latitude: Double, longitude: Double, type: String) {
        initRetrofit.getPlacesFromApiRankByDistance(
            "${latitude},${longitude}",
            "distance",
            type,
            apiKey
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(placesObserver())
    }

    fun placesByRadius(latitude: Double, longitude: Double, type: String) {
        initRetrofit.getPlacesFromApiRadius(
            "${latitude},${longitude}",
            placeRadius,
            type,
            apiKey
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(placesObserver())
    }

    private fun placesObserver():Observer<PlacesModel> {
        return object :Observer<PlacesModel>{
            override fun onComplete() {
                //hide progress indicator .
                Log.d(logTag, "placesObserver -> onComplete")
            }

            override fun onError(e: Throwable) {
                Log.d(logTag, "placesObserver -> onError: ${e.printStackTrace()}")
                places.postValue(null)
            }

            override fun onNext(placesModel: PlacesModel) {
                Log.d(logTag, "placesObserver -> onNext: ${placesModel.nextPageToken}")
                places.postValue(placesModel)
            }

            override fun onSubscribe(disposable: Disposable) {
                Log.d(logTag, "placesObserver -> onSubscribe: $disposable")
                compositeDisposable?.add(disposable)
                placesModel = PlacesModel(true, "", arrayListOf())
                places.postValue(placesModel)
                //start showing progress indicator.
            }
        }
    }


    fun placesObserverNextPage(pageToken: String) {
        initRetrofit.getPlacesNextPageFromApi(
            pageToken,
            apiKey
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(getPlaceNextPagesObserverRx())
    }

    private fun getPlaceNextPagesObserverRx():Observer<PlacesModel> {
        return object :Observer<PlacesModel>{
            override fun onComplete() {
                //hide progress indicator .
                Log.d(logTag, "onComplete")
            }

            override fun onError(e: Throwable) {
                Log.d(logTag, "onError: ${e.printStackTrace()}")
                places.postValue(null)
            }

            override fun onNext(placesModel: PlacesModel) {
                Log.d(logTag, "onNext: ${placesModel.nextPageToken}")
                places.postValue(placesModel)
            }

            override fun onSubscribe(disposable: Disposable) {
                Log.d(logTag, "onSubscribe: $disposable")
                compositeDisposable?.add(disposable)
                placesModel = PlacesModel(true, "", arrayListOf())
                places.postValue(placesModel)
                //start showing progress indicator.
            }
        }
    }
}