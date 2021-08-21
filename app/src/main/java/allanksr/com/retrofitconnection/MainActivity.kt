package allanksr.com.retrofitconnection
import allanksr.com.retrofitconnection.databinding.ActivityMainBinding
import allanksr.com.retrofitconnection.databinding.AlertChoiceBinding
import allanksr.com.retrofitconnection.viewmodel.MainActivityViewModel
import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var logTag = "logTag-MainActivity"
    @Inject
    lateinit var initLocationRequest: LocationRequest
    @Inject
    lateinit var initBlinkText: BlinkText
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var alertChoiceBinding: AlertChoiceBinding
    private var locationCode = 10001
    private var latitude: Double? =null
    private var longitude: Double? =null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var myLocation: Location
    private lateinit var resolutionForResult: ActivityResultLauncher<IntentSenderRequest>
    private val apiKey = BuildConfig.API_KEY
    private var placeTypeNames = arrayOf("Restaurants", "Bars", "Cafes")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        alertChoiceBinding = AlertChoiceBinding.inflate(layoutInflater)

        val alertDialogBuilder = android.app.AlertDialog.Builder(this)
        alertDialogBuilder.setView(alertChoiceBinding.root)
        alertDialogBuilder.setCancelable(true)
        val alertDialogChoice: android.app.AlertDialog = alertDialogBuilder.create()

        fusedLocationClient = getFusedLocationProviderClient(this)
        checkPermissions()
        enableButtons(false, View.VISIBLE)

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
                            initLocationRequest
                            checkGps()
                        }
                        .setNegativeButton("Dismiss"){ _, _ ->
                            finish()
                        }
                val permissionDialog = builder.create()
                permissionDialog.show()
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) { locationResult ?: return
                for (location in locationResult.locations){
                    latitude = location.latitude
                    longitude = location.longitude
                    myLocation = location
                    enableButtons(true, View.GONE)
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
        }

        activityMainBinding.getRestaurant.setOnClickListener{
            alertChoiceBinding.byDistance.setOnClickListener{
                getPlacesByDistance("restaurant", 0)
                alertDialogChoice.dismiss()
            }
            alertChoiceBinding.byRadius.setOnClickListener{
                getPlacesByRadius("restaurant", 0)
                alertDialogChoice.dismiss()
            }
            if(checkPermissions())
                if(apiKey.length <= 10){
                    initBlinkText.blinkTextInView(activityMainBinding.defineApiKey)
                }else{
                    alertDialogChoice.show()
                }
        }

        activityMainBinding.getBar.setOnClickListener{
            alertChoiceBinding.byDistance.setOnClickListener{
                getPlacesByDistance("bar", 1)
                alertDialogChoice.dismiss()
            }
            alertChoiceBinding.byRadius.setOnClickListener{
                getPlacesByRadius("bar", 1)
                alertDialogChoice.dismiss()
            }
            if(checkPermissions())
                if(apiKey.length <= 10){
                    initBlinkText.blinkTextInView(activityMainBinding.defineApiKey)
                }else{
                    alertDialogChoice.show()
                }
        }

        activityMainBinding.getCafe.setOnClickListener{
            alertChoiceBinding.byDistance.setOnClickListener{
                getPlacesByDistance("cafe", 2)
                alertDialogChoice.dismiss()
            }
            alertChoiceBinding.byRadius.setOnClickListener{
                getPlacesByRadius("cafe", 2)
                alertDialogChoice.dismiss()
            }
            if(checkPermissions())
                if(apiKey.length <= 10){
                    initBlinkText.blinkTextInView(activityMainBinding.defineApiKey)
                }else{
                    alertDialogChoice.show()
                }
        }

       supportFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            val result = bundle.getInt("showButtons")
            title = "Find Places"
            activityMainBinding.buttonsContainer.visibility = result
           supportFragmentManager.popBackStackImmediate()
        }

    }

    private fun enableButtons(enable: Boolean, visible: Int){
        activityMainBinding.getRestaurant.isEnabled = enable
        activityMainBinding.getBar.isEnabled = enable
        activityMainBinding.getCafe.isEnabled = enable
        activityMainBinding. restaurantLoading.visibility = visible
        activityMainBinding.barLoading.visibility = visible
        activityMainBinding.cafeLoading.visibility = visible
    }

    private fun getPlacesByDistance(placeType: String, placeName: Int){
        title = placeTypeNames[placeName]
        val bundle = bundleOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "type" to placeType,
            "radius" to false
        )
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            addToBackStack("ListPlacesFragment")
            add(activityMainBinding.fragmentContainer.id, ListPlacesFragment::class.java, bundle)
        }
        activityMainBinding.buttonsContainer.visibility = View.GONE
    }

    private fun getPlacesByRadius(placeType: String,  placeName: Int){
        title = placeTypeNames[placeName]
        val bundle = bundleOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "type" to placeType,
            "radius" to true
        )
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            addToBackStack("ListPlacesFragment")
            add(activityMainBinding.fragmentContainer.id, ListPlacesFragment::class.java, bundle)
        }
        activityMainBinding.buttonsContainer.visibility = View.GONE
    }

    private var isGpsEnabled = false
    private fun checkGps(): Boolean {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(initLocationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            isGpsEnabled = true
            startLocationUpdates()
        }
        task.addOnFailureListener{ e ->
            isGpsEnabled = false
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
        return isGpsEnabled
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askLocationPermission()
            return
        }
        fusedLocationClient.requestLocationUpdates(
                initLocationRequest,
                locationCallback,
                Looper.getMainLooper()
        )

    }

    private var isLocationEnabled = false
    private fun askLocationPermission(): Boolean  {
        isLocationEnabled = if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
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
             false
        }else{
             true
        }
        return isLocationEnabled
    }

    private fun checkPermissions(): Boolean{
        return checkGps() && askLocationPermission()
    }

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount>0){
            title = "Find Places"
            supportFragmentManager.popBackStackImmediate()
            activityMainBinding.buttonsContainer.visibility = View.VISIBLE
        }else{
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        askLocationPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(logTag, "onDestroy ")
        viewModel.compositeDisposable?.clear()
    }
}















