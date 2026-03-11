package ca.sheridancollege.medreminder.presentation.emergency

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.net.URL
import kotlin.coroutines.resume

data class NearbyHospital(
    val name: String,
    val phone: String,
    val address: String,
    val distance: Float,
    val placeId: String
)

object HospitalFinder {

    // Get device location
    suspend fun getCurrentLocation(context: Context): Location? =
        suspendCancellableCoroutine { cont ->
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location -> cont.resume(location) }
                    .addOnFailureListener { cont.resume(null) }
            } catch (e: SecurityException) {
                cont.resume(null)
            }
        }

    // Search nearby hospitals using Google Places Nearby Search API
    suspend fun findNearbyHospitals(
        lat: Double,
        lng: Double,
        apiKey: String,
        radiusMeters: Int = 5000
    ): List<NearbyHospital> {
        return try {
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=$lat,$lng" +
                "&radius=$radiusMeters" +
                "&type=hospital" +
                "&key=$apiKey"

            val response = URL(url).readText()
            val json = JSONObject(response)
            val results = json.getJSONArray("results")

            val hospitals = mutableListOf<NearbyHospital>()
            for (i in 0 until minOf(results.length(), 5)) {
                val place = results.getJSONObject(i)
                val geometry = place.getJSONObject("geometry").getJSONObject("location")
                val placeLat = geometry.getDouble("lat")
                val placeLng = geometry.getDouble("lng")

                val hospitalLocation = Location("").apply {
                    latitude = placeLat
                    longitude = placeLng
                }
                val userLocation = Location("").apply {
                    latitude = lat
                    longitude = lng
                }

                hospitals.add(NearbyHospital(
                    name = place.getString("name"),
                    phone = "", // Requires Place Details call
                    address = place.optString("vicinity", ""),
                    distance = userLocation.distanceTo(hospitalLocation),
                    placeId = place.getString("place_id")
                ))
            }
            hospitals.sortedBy { it.distance }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get phone number for a specific place
    suspend fun getHospitalPhone(placeId: String, apiKey: String): String {
        return try {
            val url = "https://maps.googleapis.com/maps/api/place/details/json" +
                "?place_id=$placeId" +
                "&fields=formatted_phone_number" +
                "&key=$apiKey"
            val response = URL(url).readText()
            val json = JSONObject(response)
            json.getJSONObject("result")
                .optString("formatted_phone_number", "")
        } catch (e: Exception) {
            ""
        }
    }

    fun formatDistance(meters: Float): String = when {
        meters < 1000 -> "${meters.toInt()}m away"
        else -> "${"%.1f".format(meters / 1000)}km away"
    }
}
