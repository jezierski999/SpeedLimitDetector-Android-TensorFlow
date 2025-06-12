package com.example.speedlimitdetector

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelStoreOwner

class LocationHelper(private val context: Context, private val viewModelStoreOwner: ViewModelStoreOwner, private val tvKMH: TextView) {

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var speedInKmph: Double = 0.0

    fun startLocationUpdates() {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                speedInKmph = location.speed * 3.6
                tvKMH.text = "%.0f km/h".format(speedInKmph)
            }

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
    }

    fun takespeed(): Double {
        return speedInKmph
    }


    fun stopLocationUpdates() {
        locationManager.removeUpdates(locationListener)
    }
}
