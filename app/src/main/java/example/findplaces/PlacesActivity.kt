package example.findplaces

import android.Manifest
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import example.findplaces.adapter.ComparatorOne
import example.findplaces.adapter.PlacesAdapter
import example.findplaces.adapter.PlacesGetterSetter
import example.findplaces.constants.Const
import example.findplaces.databinding.ActivityPlacesBinding
import example.findplaces.preferences.PreferenceProvider
import example.findplaces.viewModels.NearbyPlaceRepository
import example.findplaces.viewModels.NearbyPlaceViewModel
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList


class PlacesActivity : AppCompatActivity() {
    private var logTag = "logTag-PlacesActivity"
    private var locationCode = 10001
    private lateinit var placesBinding: ActivityPlacesBinding
    var latitude: Double? =null
    var longitude: Double? =null
    private val proximityRadius = 20000
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var nearbyPlaceViewModel: NearbyPlaceViewModel
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var myLocation: Location
    private lateinit var resolutionForResult: ActivityResultLauncher<IntentSenderRequest>
    private var lastVisiblePosition = 0
    lateinit var preferenceProvider: PreferenceProvider
    private lateinit var placesGetterSetter: MutableList<PlacesGetterSetter>
    private lateinit var placesAdapter: PlacesAdapter
    private var displayingCategory = false
    private lateinit var waitEffect: CountDownTimer
    private lateinit var comparatorOne: ComparatorOne
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        placesBinding = ActivityPlacesBinding.inflate(layoutInflater)
        setContentView(placesBinding.root)
        preferenceProvider = PreferenceProvider(this)
        fusedLocationClient = getFusedLocationProviderClient(this)

        waitEffect =  object: CountDownTimer(3000, 1000)  {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                createLocationRequest()
                settingsCheck()
            }
        }
        waitEffect.start()
        placesGetterSetter = ArrayList()

        resolutionForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK){
                Log.d(logTag, "accepted")
                startLocationUpdates()
            } else {
                Log.d(logTag, "denied")
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle("We need permission!")
                builder.setIcon(android.R.drawable.ic_menu_mylocation)
                builder.setMessage("permission is required to show places")
                    .setCancelable(false)
                    .setPositiveButton("Try again") { _, _ ->
                        createLocationRequest()
                        settingsCheck()
                    }
                    .setNegativeButton("Dismiss"){ _, _ ->
                        finish()
                    }
                val alertDialog = builder.create()
                alertDialog.show()
            }
        }

        placesBinding.getRestaurant.isEnabled = false
        placesBinding.getBar.isEnabled = false
        placesBinding.getCafe.isEnabled = false

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) { locationResult ?: return
                for (location in locationResult.locations){
                    // Update UI with location data
                    Log.d(logTag, "locationCallback: latitude ${location.latitude}")
                    Log.d(logTag, "locationCallback: longitude ${location.longitude}")
                    latitude = location.latitude
                    longitude = location.longitude
                    myLocation = location
                    placesBinding.restaurantLoading.visibility = View.GONE
                    placesBinding.barLoading.visibility = View.GONE
                    placesBinding.cafeLoading.visibility = View.GONE

                    placesBinding.getRestaurant.isEnabled = true
                    placesBinding.getBar.isEnabled = true
                    placesBinding.getCafe.isEnabled = true

                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
        }

        nearbyPlaceViewModel = ViewModelProvider(
            this,
            NearbyPlaceViewModel.MapsViewModelFactory(NearbyPlaceRepository(this))
        ).get(NearbyPlaceViewModel::class.java)

        placesBinding.getRestaurant.text = ("getRestaurant")
        placesBinding.getRestaurant.setOnClickListener{
            displayingCategory = true
            nearbyPlaceViewModel.getPlaces(latitude!!, longitude!!, proximityRadius, "restaurant")
            title = "Restaurants"
        }

        placesBinding.getBar.text = ("getBar")
        placesBinding.getBar.setOnClickListener{
            displayingCategory = true
            nearbyPlaceViewModel.getPlaces(latitude!!, longitude!!, proximityRadius, "bar")
            title = "Bars"
        }

        placesBinding.getCafe.text = ("getCafe")
        placesBinding.getCafe.setOnClickListener{
            displayingCategory = true
            nearbyPlaceViewModel.getPlaces(latitude!!, longitude!!, proximityRadius, "cafe")
            title = "Cafes"
        }

        nearbyPlaceViewModel.googlePlacesData.observe(this, { readObject ->
            if(displayingCategory)
            placesGetterSetter = convertJsonToList(readObject)
            placesAdapter = PlacesAdapter(placesGetterSetter)
            comparatorOne = ComparatorOne()
            placesGetterSetter.sortWith(comparatorOne)
            placesBinding.placeList.adapter = placesAdapter
            placesAdapter.notifyDataSetChanged()
            placesBinding.placeList.setSelection(lastVisiblePosition)
        })

        nearbyPlaceViewModel.googleMorePlacesData.observe(this, { readObject ->
            if(displayingCategory)
            placesGetterSetter = convertJsonToList(readObject)
            placesAdapter = PlacesAdapter(placesGetterSetter)
            comparatorOne = ComparatorOne()

            placesGetterSetter.sortWith(comparatorOne)
            placesBinding.placeList.adapter = placesAdapter
            placesAdapter.notifyDataSetChanged()
            placesBinding.placeList.setSelection(lastVisiblePosition)
        })

        placesBinding.placeList.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                    && placesBinding.placeList.lastVisiblePosition -
                    placesBinding.placeList.headerViewsCount -
                    placesBinding.placeList.footerViewsCount >=
                    placesBinding.placeList.count - 1
                ) {
                    lastVisiblePosition = placesBinding.placeList.lastVisiblePosition
                    if (preferenceProvider.getString(Const.pageToken).isNotEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "has more places",
                            Toast.LENGTH_SHORT
                        ).show()
                        nearbyPlaceViewModel.getMorePlaces(preferenceProvider.getString(Const.pageToken))
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "has no more places",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onScroll(
                view: AbsListView,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
            }
        })
       }

    private var placesGetter = ArrayList<PlacesGetterSetter>()
    private fun convertJsonToList(readObject: JSONObject): MutableList<PlacesGetterSetter>{
        if (readObject.has("name")) {
            val placeName = readObject.getString("name")
            val arrayNames = JSONArray(placeName)

            val openTrade = readObject.getString("open_now")
            val arrayOpenTrade = JSONArray(openTrade)

            val tradeAssessment = readObject.getString("rating")
            val arrayTradeAssessment = JSONArray(tradeAssessment)

            val lat = readObject.getString("lat")
            val latLocation = JSONArray(lat)

            val lng = readObject.getString("lng")
            val lngLocation = JSONArray(lng)

            for (i in 0 until arrayNames.length()) {
                val names = arrayNames[i] as String
                val open = arrayOpenTrade[i] as String
                val rate = arrayTradeAssessment[i] as String

                val latitude = latLocation[i] as String
                val longitude = lngLocation[i] as String

                val placeLocation = Location(names)
                placeLocation.latitude = latitude.toDouble()
                placeLocation.longitude = longitude.toDouble()

                val distanceInMeters: Float = myLocation.distanceTo(placeLocation)
                    placesGetter.add(
                        PlacesGetterSetter(
                            names,
                            open,
                            rate,
                            distanceInMeters.toInt()
                        )
                    )

            }
            placesBinding.buttonsContainer.visibility = View.GONE
        }else{
            if (readObject.has("singleValue")) {
                val placeName = readObject.getString("singleValue")
                val arrayNames = JSONArray(placeName)

                val latitude = arrayNames[0] as String
                val longitude = arrayNames[1] as String
                val names = arrayNames[2] as String
                val open = arrayNames[3] as String
                val rate = arrayNames[4] as String

                val placeLocation = Location(names)
                placeLocation.latitude = latitude.toDouble()
                placeLocation.longitude = longitude.toDouble()

                val distanceInMeters: Float = myLocation.distanceTo(placeLocation)
                //val distanceInKilometers = round(distanceInMeters)
                placesGetter.add(
                    PlacesGetterSetter(
                        names,
                        open,
                        rate,
                        distanceInMeters.toInt()
                    )
                )
                placesBinding.buttonsContainer.visibility = View.GONE
            }

        }
        return placesGetter
    }

     private fun createLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun settingsCheck() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            Log.d(logTag, "onSuccess: settingsCheck()")
            startLocationUpdates()
        }
        task.addOnFailureListener{ e ->
            if (e is ResolvableApiException) {
                Log.d(logTag, "onFailure: settingsCheck()")
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(e.resolution).build()
                    resolutionForResult.launch(intentSenderRequest)
                } catch (sendEx: SendIntentException) {
                    Log.d(logTag, "SendIntentException: ${sendEx.printStackTrace()}")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        askLocationPermission()
        Log.d(logTag, "onStart askLocationPermission()")
    }

    private fun askLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )) {
                Log.d(logTag, "askLocationPermission: you should show an alert dialog...")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    locationCode
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    locationCode
                )
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askLocationPermission()
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onBackPressed() {
        if(displayingCategory){
            title = "Find Places"
            displayingCategory = false
            lastVisiblePosition = 0
            placesGetterSetter.clear()
            placesAdapter.notifyDataSetChanged()
            placesBinding.buttonsContainer.visibility = View.VISIBLE
        }else{
            waitEffect.cancel()
            finish()
        }
    }

}



