package allanksr.com.retrofitconnection

import allanksr.com.retrofitconnection.adapter.DistanceComparator
import allanksr.com.retrofitconnection.adapter.PlacesAdapter
import allanksr.com.retrofitconnection.adapter.PlacesDistance
import allanksr.com.retrofitconnection.databinding.ListPlaceFragmentBinding
import allanksr.com.retrofitconnection.viewmodel.MainActivityViewModel
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListPlacesFragment : Fragment()  {
    private var logTag = "logTag-ListPlacesFragment"
    private lateinit var listPlaceFragmentBinding: ListPlaceFragmentBinding
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var placesDistance: ArrayList<PlacesDistance>
    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var placeLocation : Location
    private lateinit var distanceComparator: DistanceComparator
    private var lastVisiblePosition = 0
    private var latitude : Double? = null
    private var longitude : Double? = null
    private var type : String? = null
    private var nextPageToken : String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View {
        listPlaceFragmentBinding = ListPlaceFragmentBinding.inflate(inflater, container, false)
        listPlaceFragmentBinding.recyclerView.apply {
            placesDistance = ArrayList()
            layoutManager = LinearLayoutManager(activity)
            placesAdapter = PlacesAdapter()
            adapter = placesAdapter
        }

        latitude = requireArguments().getDouble("latitude")
        longitude = requireArguments().getDouble("longitude")
        type = requireArguments().getString("type")
        if(requireArguments().getBoolean("radius")){
            viewModel.placesByRadius(
                    latitude!!,
                    longitude!!,
                    type!!
            )
        }else{
            viewModel.placesByDistance(
                    latitude!!,
                    longitude!!,
                    type!!
            )
        }
        return listPlaceFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.run {
            viewModel.getPlacesObserver().observe(this, {
                if (it != null) {

                    if (it.loading) {
                        Log.d(logTag, "loading:  ${it.loading}")
                    }else{
                        Log.d(logTag, "loading:  ${it.loading}")
                        if(it.resultsArray.size > 0){
                            if (it.nextPageToken != null) {
                                nextPageToken = it.nextPageToken!!
                            } else {
                                nextPageToken = ""
                            }
                            for (a in it.resultsArray.iterator()) {
                                placeLocation = Location(a.name)
                                placeLocation.latitude = a.geometry?.location?.lat!!
                                placeLocation.longitude = a.geometry?.location?.lng!!
                                val results = FloatArray(1)
                                Location.distanceBetween(latitude!!, longitude!!,
                                    placeLocation.latitude, placeLocation.longitude, results)
                                val distance = results[0]

                                Log.d(logTag, "distance $distance")

                                placesDistance.add(
                                    PlacesDistance(
                                        distance.toInt()
                                    )
                                )
                            }
                            distanceComparator = DistanceComparator()
                            placesDistance.sortWith(distanceComparator)
                            placesAdapter.placesData.addAll(it.resultsArray)
                            placesAdapter.distanceComparator.addAll(placesDistance)
                            placesAdapter.notifyDataSetChanged()
                            listPlaceFragmentBinding.recyclerView.scrollToPosition(lastVisiblePosition)
                            listPlaceFragmentBinding.progressBar.visibility = View.GONE
                        }else{
                            Toast.makeText(view.context, "error, no values to be shown || you can increase radius ", Toast.LENGTH_SHORT).show()
                            setFragmentResult("requestKey", bundleOf("showButtons" to View.VISIBLE))
                        }
                    }


                }
            })
        }

        listPlaceFragmentBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(view: RecyclerView, scrollState: Int) {
                super.onScrollStateChanged(view, scrollState)
                if (!listPlaceFragmentBinding.recyclerView.canScrollVertically(1) && scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d(logTag, "end--------------------------")

                    val manager = (listPlaceFragmentBinding.recyclerView.layoutManager as LinearLayoutManager)
                    lastVisiblePosition = manager.findLastCompletelyVisibleItemPosition()+1
                    Log.d(logTag, "lastVisiblePosition:  $lastVisiblePosition")

                    if (nextPageToken!!.isNotEmpty()) {
                        listPlaceFragmentBinding.progressBar.visibility = View.VISIBLE
                        viewModel.placesObserverNextPage(
                                nextPageToken!!
                        )
                        Toast.makeText(view.context, "Loading more", Toast.LENGTH_SHORT).show()
                    }else{
                        listPlaceFragmentBinding.recyclerView.removeOnScrollListener(this)
                        Toast.makeText(view.context, "there are no more places", Toast.LENGTH_SHORT).show()
                    }


                }
            }
        })
    }

    override fun onDestroyView() {
        Log.d(logTag, "onDestroyView")
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
        super.onDestroyView()
    }
}