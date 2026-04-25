package ca.sheridancollege.medreminder.data.local.converter

import androidx.room.TypeConverter
import ca.sheridancollege.medreminder.domain.model.MedicationTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MedicationConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromMedicationTimeList(value: List<MedicationTime>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMedicationTimeList(value: String): List<MedicationTime> {
        val listType = object : TypeToken<List<MedicationTime>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}
