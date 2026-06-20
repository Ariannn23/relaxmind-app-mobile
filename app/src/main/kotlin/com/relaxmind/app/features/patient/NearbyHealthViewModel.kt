package com.relaxmind.app.features.patient

import android.annotation.SuppressLint
import android.app.Application
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.relaxmind.app.data.model.NearbyHealthCenter
import com.relaxmind.app.data.remote.MapsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserLocation(val lat: Double, val lng: Double)

class NearbyHealthViewModel(application: Application) : AndroidViewModel(application) {

    private val mapsService = MapsService(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _healthCenters = MutableStateFlow<List<NearbyHealthCenter>>(emptyList())
    val healthCenters: StateFlow<List<NearbyHealthCenter>> = _healthCenters.asStateFlow()

    private val _userLocation = MutableStateFlow<UserLocation?>(null)
    val userLocation: StateFlow<UserLocation?> = _userLocation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow<Boolean?>(null)
    val hasLocationPermission: StateFlow<Boolean?> = _hasLocationPermission.asStateFlow()

    private var isFirstFetchDone = false
    private var searchRadius = 5000

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation ?: return
            _userLocation.value = UserLocation(location.latitude, location.longitude)
            
            // Fetch centers only once per radius expansion to avoid API spamming
            if (!isFirstFetchDone) {
                fetchNearbyCenters(location.latitude, location.longitude)
                isFirstFetchDone = true
            }
        }
    }

    fun onPermissionGranted() {
        _hasLocationPermission.value = true
        startLocationUpdates()
    }

    fun onPermissionDenied() {
        _hasLocationPermission.value = false
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun fetchNearbyCenters(lat: Double, lng: Double) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = mapsService.getNearbyHealthCenters(lat, lng, searchRadius)
            result.onSuccess { centers ->
                _healthCenters.value = centers
            }.onFailure {
                // Ignore error locally, could show a toast or error state
            }
            _isLoading.value = false
        }
    }

    fun searchWiderArea() {
        searchRadius *= 2
        val loc = _userLocation.value
        if (loc != null) {
            fetchNearbyCenters(loc.lat, loc.lng)
        }
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
