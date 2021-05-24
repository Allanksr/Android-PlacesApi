package allanksr.com.retrofitconnection

import allanksr.com.retrofitconnection.adapter.DistanceComparator
import allanksr.com.retrofitconnection.adapter.PlacesAdapter
import allanksr.com.retrofitconnection.adapter.PlacesDistance
import allanksr.com.retrofitconnection.preferences.PreferenceProvider
import allanksr.com.retrofitconnection.viewmodel.MainActivityViewModel
import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var logTag = "logTag-MainActivity"
    private var apiKey = "api_key"
    private var locationCode = 10001
    var latitude: Double? =null
    var longitude: Double? =null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    lateinit var placeLocation : Location
    private lateinit var myLocation: Location
    private lateinit var resolutionForResult: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var placesDistance: ArrayList<PlacesDistance>
    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var distanceComparator: DistanceComparator
    private lateinit var preferenceProvider: PreferenceProvider
    private var displayingCategory = false
    private var lastVisiblePosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferenceProvider = PreferenceProvider(this)
        fusedLocationClient = getFusedLocationProviderClient(this)
        createLocationRequest()
        settingsCheck()

        getRestaurant.isEnabled = false
        getBar.isEnabled = false
        getCafe.isEnabled = false

        recyclerView.apply {
            placesDistance = ArrayList()
            layoutManager = LinearLayoutManager(context)
            placesAdapter = PlacesAdapter()
            adapter = placesAdapter
        }



        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        viewModel.getPlacesObserver().observe(this, {
            if (displayingCategory)
                if (it != null) {
                    if (it.nextPageToken != null) {
                        preferenceProvider.setString(
                            "pageToken",
                            it.nextPageToken!!
                        )
                    } else {
                        preferenceProvider.setString(
                            "pageToken",
                            ""
                        )
                    }

                    for (a in it.resultsArray.iterator()) {
                        placeLocation = Location(a.name)
                        placeLocation.latitude = a.geometry?.location?.lat!!
                        placeLocation.longitude = a.geometry?.location?.lng!!

                        Log.d(logTag, "${a.name}")
                        Log.d(logTag, "${placeLocation.latitude}")
                        Log.d(logTag, "${placeLocation.longitude}")

                        val distanceInMeters: Float = myLocation.distanceTo(placeLocation)
                        placesDistance.add(
                            PlacesDistance(
                                distanceInMeters.toInt()
                            )
                        )
                    }

                    placesAdapter.placesData.addAll(it.resultsArray)
                    distanceComparator = DistanceComparator()
                    placesDistance.sortWith(distanceComparator)
                    placesAdapter.distanceComparator.addAll(placesDistance)
                    placesAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(lastVisiblePosition)
                    buttonsContainer.visibility = View.GONE
                } else {
                    Toast.makeText(this, "Error in fetching data", Toast.LENGTH_SHORT).show()
                }
        })


        resolutionForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK){
                Log.d(logTag, "accepted")
                restaurantLoading.visibility = View.VISIBLE
                barLoading.visibility = View.VISIBLE
                cafeLoading.visibility = View.VISIBLE
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

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) { locationResult ?: return
                for (location in locationResult.locations){
                    Log.d(logTag, "locationCallback: latitude ${location.latitude}")
                    Log.d(logTag, "locationCallback: longitude ${location.longitude}")
                    latitude = location.latitude
                    longitude = location.longitude
                    myLocation = location

                    getRestaurant.isEnabled = true
                    getBar.isEnabled = true
                    getCafe.isEnabled = true

                    restaurantLoading.visibility = View.GONE
                    barLoading.visibility = View.GONE
                    cafeLoading.visibility = View.GONE

                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
        }


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(view: RecyclerView, scrollState: Int) {
                super.onScrollStateChanged(view, scrollState)
                if (!recyclerView.canScrollVertically(1) && scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d(logTag, "end--------------------------")

                    val manager = (recyclerView.layoutManager as LinearLayoutManager)
                    lastVisiblePosition = manager.findLastCompletelyVisibleItemPosition()+1
                    Log.d(logTag, "lastVisiblePosition:  $lastVisiblePosition")

                    if (preferenceProvider.getString("pageToken").isNotEmpty()) {
                        Toast.makeText(this@MainActivity, "Loading more", Toast.LENGTH_SHORT).show()
                        viewModel.getPlacesNextPage(
                            preferenceProvider.getString("pageToken"),
                            apiKey
                        )
                    }
                    Toast.makeText(this@MainActivity, "there are no more places", Toast.LENGTH_SHORT).show()


                }
            }
        })


        getRestaurant.setOnClickListener{
            displayingCategory = true
            title = "Restaurants"
            viewModel.getPlacesObject(
                latitude!!,
                longitude!!,
                "restaurant",
                apiKey
            )
        }

        getBar.setOnClickListener{
            title = "Bars"
            displayingCategory = true
            viewModel.getPlacesObject(
                latitude!!,
                longitude!!,
                "bar",
                apiKey
            )
        }

        getCafe.setOnClickListener{
            title = "Cafes"
            displayingCategory = true
            viewModel.getPlacesObject(
                latitude!!,
                longitude!!,
                "cafe",
                apiKey
            )
        }



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
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(logTag, "SendIntentException: ${sendEx.printStackTrace()}")
                }
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

    override fun onBackPressed() {
        if(displayingCategory){
            title = "Find Places"
            displayingCategory = false
            placesAdapter.placesData = arrayListOf()
            placesAdapter.notifyDataSetChanged()
            buttonsContainer.visibility = View.VISIBLE
        }else{
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        askLocationPermission()
        Log.d(logTag, "onStart askLocationPermission()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(logTag, "onDestroy ")
        viewModel.compositeDisposable?.clear()
    }

}















