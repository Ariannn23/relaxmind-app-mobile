package com.relaxmind.app.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.relaxmind.app.data.model.NearbyHealthCenter
import kotlinx.coroutines.tasks.await

class MapsService(private val context: Context) {

    private val placesClient: PlacesClient by lazy {
        Places.createClient(context)
    }

    @SuppressLint("MissingPermission")
    suspend fun getNearbyHealthCenters(lat: Double, lng: Double, radiusMeters: Int): Result<List<NearbyHealthCenter>> {
        return try {
            val mockCenters = listOf(
                NearbyHealthCenter(
                    id = "mock_1",
                    name = "Hospital Regional Docente de Trujillo",
                    address = "Av. Mansiche 795, Trujillo",
                    lat = -8.106544,
                    lng = -79.034515,
                    phoneNumber = "+51 44 481200",
                    distanceMeters = calculateDistance(lat, lng, -8.106544, -79.034515),
                    schedule = "24/7",
                    rating = 4.0,
                    isPublic = true,
                    typeName = "MINSA"
                ),
                NearbyHealthCenter(
                    id = "mock_2",
                    name = "Hospital Belén de Trujillo",
                    address = "Jirón Bolívar Nº 350, Trujillo",
                    lat = -8.115629,
                    lng = -79.028681,
                    phoneNumber = "+51 44 480200",
                    distanceMeters = calculateDistance(lat, lng, -8.115629, -79.028681),
                    schedule = "24/7",
                    rating = 4.2,
                    isPublic = true,
                    typeName = "MINSA"
                ),
                NearbyHealthCenter(
                    id = "mock_3",
                    name = "Hospital de Alta Complejidad \"Virgen de la Puerta\"",
                    address = "Av. Micaela Bastidas 309, La Esperanza",
                    lat = -8.077222,
                    lng = -79.043611,
                    phoneNumber = "+51 44 480860",
                    distanceMeters = calculateDistance(lat, lng, -8.077222, -79.043611),
                    schedule = "24/7",
                    rating = 4.5,
                    isPublic = true,
                    typeName = "EsSalud"
                ),
                NearbyHealthCenter(
                    id = "mock_4",
                    name = "Hospital Víctor Lazarte Echegaray",
                    address = "Prol. Unión 1375, Trujillo",
                    lat = -8.118056,
                    lng = -79.020278,
                    phoneNumber = "+51 44 216119",
                    distanceMeters = calculateDistance(lat, lng, -8.118056, -79.020278),
                    schedule = "24/7",
                    rating = 3.9,
                    isPublic = true,
                    typeName = "EsSalud"
                ),
                NearbyHealthCenter(
                    id = "mock_5",
                    name = "Clínica San Pablo Trujillo",
                    address = "Av. Húsares de Junín 690, Urb. La Merced",
                    lat = -8.125833,
                    lng = -79.031944,
                    phoneNumber = "+51 44 485244",
                    distanceMeters = calculateDistance(lat, lng, -8.125833, -79.031944),
                    schedule = "24/7",
                    rating = 4.6,
                    isPublic = false,
                    typeName = "Privado"
                ),
                NearbyHealthCenter(
                    id = "mock_6",
                    name = "Clínica Peruano Americana",
                    address = "Av. Mansiche 810, Trujillo",
                    lat = -8.106111,
                    lng = -79.035278,
                    phoneNumber = "+51 44 242400",
                    distanceMeters = calculateDistance(lat, lng, -8.106111, -79.035278),
                    schedule = "Lun-Vie: 8am-8pm",
                    rating = 4.3,
                    isPublic = false,
                    typeName = "Privado"
                ),
                NearbyHealthCenter(
                    id = "mock_7",
                    name = "Hospital La Noria",
                    address = "Av. Blas Pascal 124, Trujillo",
                    lat = -8.115278,
                    lng = -79.013611,
                    phoneNumber = "+51 44 317622",
                    distanceMeters = calculateDistance(lat, lng, -8.115278, -79.013611),
                    schedule = "24/7",
                    rating = 3.8,
                    isPublic = true,
                    typeName = "MINSA"
                ),
                NearbyHealthCenter(
                    id = "mock_8",
                    name = "Hospital Distrital Vista Alegre",
                    address = "Manuel Seoane 1008, Víctor Larco Herrera",
                    lat = -8.134722,
                    lng = -79.038056,
                    phoneNumber = null,
                    distanceMeters = calculateDistance(lat, lng, -8.134722, -79.038056),
                    schedule = "24/7",
                    rating = 3.5,
                    isPublic = true,
                    typeName = "MINSA"
                )
            ).sortedBy { it.distanceMeters }

            Result.success(mockCenters)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Int {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0].toInt()
    }
}
