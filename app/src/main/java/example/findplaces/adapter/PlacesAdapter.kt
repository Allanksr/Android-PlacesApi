package  example.findplaces.adapter

import  example.findplaces.databinding.PlacesListBinding
import  example.findplaces.databinding.PlacesListBinding.inflate
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import java.util.*


class PlacesAdapter(private val listStorage: MutableList<PlacesGetterSetter>) : BaseAdapter() {
    private val arrayList: ArrayList<PlacesGetterSetter> = ArrayList()
    private lateinit var placesListBinding: PlacesListBinding
    private lateinit var relativeLayout: RelativeLayout
    init {
        this.arrayList.addAll(listStorage)
    }

    override fun getCount(): Int {
        return listStorage.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, cView: View?, parent: ViewGroup): View {
        placesListBinding = inflate(LayoutInflater.from(parent.context), parent, false)
        relativeLayout = placesListBinding.root
        placesListBinding.placeName.text = listStorage[position].placeName

        when (listStorage[position].openTrade) {
            "true" -> {
                placesListBinding.openTrade.text = ("Open now")
            }
            "false" -> {
                placesListBinding.openTrade.text = ("Closed")
            }
            else -> {
                placesListBinding.openTrade.text = ("Open/Closed :Information not avaliable")
            }
        }

        when (listStorage[position].tradeAssessment) {
            "undefined" -> {
                placesListBinding.tradeAssessment.text = ("Rated :Information not avaliable")
            }
            else -> {
                placesListBinding.tradeAssessment.text = ("Rated :${listStorage[position].tradeAssessment}")
            }
        }

        placesListBinding.placeDistance.text = ("${listStorage[position].placeDistance}: meters away")

     return placesListBinding.root
    }

}
