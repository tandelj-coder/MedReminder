package ca.sheridancollege.medreminder.presentation.emergency

import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.sheridancollege.medreminder.data.datastore.UserPreferencesDataStore
import ca.sheridancollege.medreminder.data.repository.MedicationRepository
import ca.sheridancollege.medreminder.domain.model.Medication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmergencyUiState(
    val emergencyName: String = "",
    val emergencyPhone: String = "",
    val emergencyRelation: String = "",
    val bloodType: String = "",
    val conditions: String = "",
    val allergies: String = "",
    val medications: List<Medication> = emptyList(),
    val nearbyHospitals: List<NearbyHospital> = emptyList(),
    val isLoadingHospitals: Boolean = false,
    val locationError: String? = null
)

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
    private val repository: MedicationRepository
) : ViewModel() {

    private val _hospitals = MutableStateFlow<List<NearbyHospital>>(emptyList())
    private val _isLoadingHospitals = MutableStateFlow(false)
    private val _locationError = MutableStateFlow<String?>(null)

    private val PLACES_API_KEY = "YOUR_GOOGLE_PLACES_API_KEY"

    val uiState: StateFlow<EmergencyUiState> = combine(
        combine(
            dataStore.emergencyContactName,
            dataStore.emergencyContactPhone,
            dataStore.emergencyContactRelation,
            dataStore.userBloodType,
            dataStore.userMedicalConditions
        ) { name, phone, relation, blood, conditions ->
            listOf(name, phone, relation, blood, conditions)
        },
        dataStore.userAllergies,
        repository.getAllActiveMedications()
    ) { baseInfo, allergies, meds ->
        EmergencyUiState(
            emergencyName = baseInfo[0],
            emergencyPhone = baseInfo[1],
            emergencyRelation = baseInfo[2],
            bloodType = baseInfo[3],
            conditions = baseInfo[4],
            allergies = allergies,
            medications = meds
        )
    }.combine(_hospitals) { base, hospitals ->
        base.copy(nearbyHospitals = hospitals)
    }.combine(_isLoadingHospitals) { base, loading ->
        base.copy(isLoadingHospitals = loading)
    }.combine(_locationError) { base, error ->
        base.copy(locationError = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EmergencyUiState())

    fun saveEmergencyContact(name: String, phone: String, relation: String) {
        viewModelScope.launch { dataStore.setEmergencyContact(name, phone, relation) }
    }

    fun saveMedicalInfo(bloodType: String, conditions: String, allergies: String) {
        viewModelScope.launch { dataStore.setMedicalInfo(bloodType, conditions, allergies) }
    }

    fun findNearbyHospitals(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingHospitals.value = true
            _locationError.value = null
            try {
                val location = HospitalFinder.getCurrentLocation(context)
                if (location == null) {
                    _locationError.value = "Could not get location. Please enable GPS."
                    _isLoadingHospitals.value = false
                    return@launch
                }
                _hospitals.value = if (PLACES_API_KEY == "YOUR_GOOGLE_PLACES_API_KEY") {
                    getMockHospitals(location)
                } else {
                    val hospitals = HospitalFinder.findNearbyHospitals(
                        location.latitude, location.longitude, PLACES_API_KEY
                    )
                    hospitals.map { h ->
                        if (h.phone.isBlank() && h.placeId.isNotBlank())
                            h.copy(phone = HospitalFinder.getHospitalPhone(h.placeId, PLACES_API_KEY))
                        else h
                    }
                }
            } catch (e: Exception) {
                _locationError.value = "Error finding hospitals: ${e.message}"
            } finally {
                _isLoadingHospitals.value = false
            }
        }
    }

    private fun getMockHospitals(location: Location): List<NearbyHospital> {
        val regions = listOf(
            Triple(43.4675, -79.6877, listOf(
                NearbyHospital("Oakville Trafalgar Memorial Hospital", "(905) 845-2571", "3001 Hospital Gate, Oakville", 0f, ""),
                NearbyHospital("Credit Valley Hospital", "(905) 813-2200", "2200 Eglinton Ave W, Mississauga", 0f, ""),
                NearbyHospital("St. Joseph's Health Centre", "(416) 530-6000", "30 The Queensway, Toronto", 0f, "")
            )),
            Triple(43.6532, -79.3832, listOf(
                NearbyHospital("Toronto General Hospital", "(416) 340-4800", "200 Elizabeth St, Toronto", 0f, ""),
                NearbyHospital("St. Michael's Hospital", "(416) 360-4000", "36 Queen St E, Toronto", 0f, ""),
                NearbyHospital("Sunnybrook Hospital", "(416) 480-6100", "2075 Bayview Ave, Toronto", 0f, "")
            )),
            Triple(43.7315, -79.7624, listOf(
                NearbyHospital("Brampton Civic Hospital", "(905) 494-2120", "2100 Bovaird Dr E, Brampton", 0f, ""),
                NearbyHospital("Credit Valley Hospital", "(905) 813-2200", "2200 Eglinton Ave W, Mississauga", 0f, "")
            )),
            Triple(49.2827, -123.1207, listOf(
                NearbyHospital("Vancouver General Hospital", "(604) 875-4111", "855 W 12th Ave, Vancouver", 0f, ""),
                NearbyHospital("St. Paul's Hospital", "(604) 682-2344", "1081 Burrard St, Vancouver", 0f, "")
            )),
            Triple(51.0447, -114.0719, listOf(
                NearbyHospital("Foothills Medical Centre", "(403) 944-1110", "1403 29 St NW, Calgary", 0f, ""),
                NearbyHospital("Peter Lougheed Centre", "(403) 943-4555", "3500 26 Ave NE, Calgary", 0f, "")
            )),
            Triple(45.4215, -75.6919, listOf(
                NearbyHospital("The Ottawa Hospital", "(613) 722-7000", "1053 Carling Ave, Ottawa", 0f, ""),
                NearbyHospital("Montfort Hospital", "(613) 746-4621", "713 Montreal Rd, Ottawa", 0f, "")
            )),
            Triple(45.5017, -73.5673, listOf(
                NearbyHospital("Montreal General Hospital", "(514) 934-1934", "1650 Cedar Ave, Montreal", 0f, ""),
                NearbyHospital("Royal Victoria Hospital", "(514) 934-1934", "1001 Decarie Blvd, Montreal", 0f, "")
            ))
        )

        val nearest = regions.minByOrNull { (lat, lng, _) ->
            Location("").apply { latitude = lat; longitude = lng }
                .distanceTo(location)
        }

        return nearest?.third?.mapIndexed { i, h ->
            val ref = Location("").apply {
                latitude = nearest.first + (i * 0.015)
                longitude = nearest.second + (i * 0.015)
            }
            h.copy(distance = location.distanceTo(ref))
        }?.sortedBy { it.distance } ?: listOf(
            NearbyHospital("Emergency Services", "911", "Call for nearest hospital", 0f, "")
        )
    }

    fun sendEmergencySms(context: Context) {
        val state = uiState.value
        if (state.emergencyPhone.isBlank()) return
        val medList = state.medications.joinToString("\n") { "• ${it.name} ${it.dosage} at ${it.formattedTime()}" }
        val message = """
🚨 EMERGENCY ALERT from MedReminder
🩸 Blood Type: ${state.bloodType.ifBlank { "Unknown" }}
🏥 Conditions: ${state.conditions.ifBlank { "None" }}
⚠️ Allergies: ${state.allergies.ifBlank { "None" }}
💊 Medications:
$medList
Please help immediately.
        """.trimIndent()
        try {
            val smsManager = context.getSystemService(android.telephony.SmsManager::class.java)
            smsManager.sendMultipartTextMessage(state.emergencyPhone, null,
                smsManager.divideMessage(message), null, null)
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_SENDTO,
                android.net.Uri.parse("smsto:${state.emergencyPhone}")).apply {
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    fun shareMedicationList(context: Context) {
        val state = uiState.value
        val medList = state.medications.joinToString("\n") {
            "• ${it.name} — ${it.dosage} — ${it.formattedTime()}"
        }
        val text = """
MEDICAL INFORMATION CARD — MedReminder
🩸 Blood Type: ${state.bloodType.ifBlank { "Unknown" }}
🏥 Conditions: ${state.conditions.ifBlank { "None" }}
⚠️ Allergies: ${state.allergies.ifBlank { "None" }}
💊 MEDICATIONS:
$medList
Emergency Contact: ${state.emergencyName} (${state.emergencyRelation}) — ${state.emergencyPhone}
        """.trimIndent()
        context.startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_SUBJECT, "My Medical Information")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }, "Share Medical Info"
        ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
    }
}
