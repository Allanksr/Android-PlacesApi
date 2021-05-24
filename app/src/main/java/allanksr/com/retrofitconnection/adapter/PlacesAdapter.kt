package allanksr.com.retrofitconnection.adapter

import allanksr.com.retrofitconnection.R
import allanksr.com.retrofitconnection.network.PlacesArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlacesAdapter: RecyclerView.Adapter<PlacesAdapter.MyViewHolder>() {

    var placesData = ArrayList<PlacesArray>()
    var distanceComparator = ArrayList<PlacesDistance>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
       val inflater = LayoutInflater.from(parent.context).inflate(
           R.layout.recycler_list_row,
           parent,
           false
       )
        return MyViewHolder(inflater)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.bind(placesData[position], distanceComparator[position])
    }

    override fun getItemCount(): Int {
        return placesData.size
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val placeName: TextView = view.findViewById(R.id.placeName)
        private val openTrade: TextView = view.findViewById(R.id.openTrade)
        private val tradeAssessment: TextView = view.findViewById(R.id.tradeAssessment)
        private val placeDistance: TextView = view.findViewById(R.id.placeDistance)


        fun bind(data: PlacesArray, distanceComparator: PlacesDistance){

            placeName.text = data.name

            when (data.openingHours?.openNow) {
                true-> {
                    openTrade.text = ("Open now")
                }
                false -> {
                    openTrade.text = ("Closed")
                }
                else -> {
                    openTrade.text = ("Open/Closed :Information not avaliable")
                }
            }

            when (data.rating!= null) {
                true-> {
                    tradeAssessment.text = ("Rated :${data.rating}")
                }
                else -> {
                    tradeAssessment.text = ("Rated :Information not avaliable")
                }
            }

            placeDistance.text = ("${distanceComparator.placeDistance}: meters away")
        }
    }
}