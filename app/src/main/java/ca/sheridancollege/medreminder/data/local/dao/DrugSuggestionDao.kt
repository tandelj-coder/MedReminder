package ca.sheridancollege.medreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import ca.sheridancollege.medreminder.data.local.entity.DrugSuggestionEntity

@Dao
interface DrugSuggestionDao {
    @Insert
    suspend fun insertSuggestion(suggestion: DrugSuggestionEntity)

    @Insert
    suspend fun insertSuggestions(suggestions: List<DrugSuggestionEntity>)

    @Query("SELECT drugName FROM drug_suggestions WHERE searchQuery = :query")
    suspend fun getSuggestionsForQuery(query: String): List<String>

    @Query("DELETE FROM drug_suggestions WHERE cachedAt < :cutoffTime")
    suspend fun deleteOldCache(cutoffTime: Long)

    @Query("DELETE FROM drug_suggestions WHERE searchQuery = :query")
    suspend fun deleteSuggestionsForQuery(query: String)

    @Query("SELECT COUNT(*) FROM drug_suggestions WHERE searchQuery = :query")
    suspend fun hasCachedSuggestions(query: String): Int
}
