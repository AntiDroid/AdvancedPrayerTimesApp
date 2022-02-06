package com.example.advancedprayertimes.logic.util

import android.content.Context
import android.location.Address
import kotlin.Throws
import android.location.Geocoder
import java.lang.Exception
import java.util.*

object LocationUtil
{
    @JvmStatic
    @Throws(Exception::class)
    fun RetrieveCityByLocation(context: Context?, longitude: Double, latitude: Double): Address?
    {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 5)

        return if (Geocoder.isPresent() && addresses.size > 0)
        {
            addresses[0]
        }
        else null
    }

    @Throws(Exception::class)
    fun RetrieveCitiesByName(context: Context?, name: String?): List<Address>
    {
        val geocoder = Geocoder(context)
        return geocoder.getFromLocationName(name, 5)
    }
}