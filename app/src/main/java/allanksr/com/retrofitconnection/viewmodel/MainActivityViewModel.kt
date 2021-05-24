package allanksr.com.retrofitconnection.viewmodel

import allanksr.com.retrofitconnection.network.PlacesModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import allanksr.com.retrofitconnection.network.RetroInstance
import allanksr.com.retrofitconnection.network.RetroService
import android.util.Log
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainActivityViewModel: ViewModel() {
    private var logTag = "logTag-MainActivityViewModel"
    var compositeDisposable: CompositeDisposable? = null
    var places: MutableLiveData<PlacesModel> = MutableLiveData()

    fun getPlacesObserver(): MutableLiveData<PlacesModel> {
        return places
    }

    fun getPlacesObject(latitude: Double, longitude: Double, proximityRadius: Int, type: String, key: String) {
        val retroInstance  = RetroInstance.getRetroInstance().create(RetroService::class.java)
        retroInstance.getPlacesFromApi(
               "${latitude},${longitude}",
                proximityRadius,
                type,
                key
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(getPlacesObserverRx())
    }

    private fun getPlacesObserverRx():Observer<PlacesModel> {
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
                //start showing progress indicator.
            }
        }
    }


    fun getPlacesNextPage(pageToken: String, key: String) {
        val retroInstance  = RetroInstance.getRetroInstance().create(RetroService::class.java)
        retroInstance.getPlacesNextPageFromApi(
            pageToken,
            key
        )
            .subscribeOn(Schedulers.io())
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
                //start showing progress indicator.
            }
        }
    }
}