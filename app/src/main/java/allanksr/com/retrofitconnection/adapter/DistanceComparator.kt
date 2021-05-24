package allanksr.com.retrofitconnection.adapter

class DistanceComparator: Comparator<PlacesDistance>{
    override fun compare(c1: PlacesDistance, c2: PlacesDistance): Int {
        return c1.placeDistance - c2.placeDistance
    }
}