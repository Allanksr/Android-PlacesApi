package example.findplaces.adapter

class ComparatorOne: Comparator<PlacesGetterSetter>{
    override fun compare(c1: PlacesGetterSetter, c2: PlacesGetterSetter): Int {
        return c1.placeDistance - c2.placeDistance
    }
}