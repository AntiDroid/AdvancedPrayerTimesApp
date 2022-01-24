package com.example.advancedprayertimes.logic

import android.location.Address
import com.google.android.libraries.places.api.model.Place

class CustomPlaceEntity(id: String?, latitude: Double, longitude: Double, name: String?) {
    var id: String? = null
    var name: String? = null
    var location: CustomLocation? = null

    constructor(place: Place) : this(
        place.id,
        place.latLng.latitude,
        place.latLng.longitude,
        place.name
    ) {
    }

    constructor(address: Address) : this(
        null,
        address.latitude,
        address.longitude,
        address.locality
    ) {
    }

    override fun toString(): String {
        return name!!
    }

    init {
        this.id = id
        location = CustomLocation(longitude, latitude, "")
        this.name = name
    }
}