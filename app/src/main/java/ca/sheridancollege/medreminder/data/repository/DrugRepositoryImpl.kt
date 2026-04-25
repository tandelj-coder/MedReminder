package ca.sheridancollege.medreminder.data.repository

import ca.sheridancollege.medreminder.data.local.dao.DrugSuggestionDao
import ca.sheridancollege.medreminder.data.local.entity.DrugSuggestionEntity
import ca.sheridancollege.medreminder.domain.model.DrugSuggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrugRepositoryImpl @Inject constructor(
    private val drugSuggestionDao: DrugSuggestionDao
) : DrugRepository {

    companion object {
        private const val CACHE_VALIDITY_MS = 7 * 24 * 60 * 60 * 1000L // 7 days
    }

    override suspend fun searchDrugs(query: String): Result<List<DrugSuggestion>> =
        withContext(Dispatchers.IO) {
            try {
                val lowerQuery = query.lowercase().trim()

                // Step 1: Check local cache first
                val cached = drugSuggestionDao.getSuggestionsForQuery(lowerQuery)
                if (cached.isNotEmpty()) {
                    return@withContext Result.success(
                        cached.map { DrugSuggestion(name = it) }
                    )
                }

                // Step 2: Query network
                val suggestions = queryRxNormApi(lowerQuery)
                if (suggestions.isSuccess) {
                    val drugs = suggestions.getOrDefault(emptyList())
                    // Cache the results
                    if (drugs.isNotEmpty()) {
                        cacheResults(lowerQuery, drugs)
                    }
                }

                suggestions
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private suspend fun queryRxNormApi(query: String): Result<List<DrugSuggestion>> {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://rxnav.nlm.nih.gov/REST/spellingsuggestions.json?name=$encoded")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return Result.failure(
                    Exception("RxNorm API error: HTTP $responseCode")
                )
            }

            val body = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            val suggestions = parseSpellingSuggestions(body)
            Result.success(suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun cacheResults(query: String, drugs: List<DrugSuggestion>) {
        try {
            val entities = drugs.map { drug ->
                DrugSuggestionEntity(
                    searchQuery = query,
                    drugName = drug.name,
                    cachedAt = System.currentTimeMillis()
                )
            }
            drugSuggestionDao.insertSuggestions(entities)
            // Cleanup old cache
            cleanupOldCache()
        } catch (e: Exception) {
            // Silent fail on cache write
        }
    }

    private suspend fun cleanupOldCache() {
        try {
            val cutoff = System.currentTimeMillis() - CACHE_VALIDITY_MS
            drugSuggestionDao.deleteOldCache(cutoff)
        } catch (e: Exception) {
            // Silent fail on cleanup
        }
    }

    private fun parseSpellingSuggestions(json: String): List<DrugSuggestion> {
        return try {
            val root = JSONObject(json)
            val suggestionList = root
                .getJSONObject("suggestionGroup")
                .optJSONObject("suggestionList")
                ?: return emptyList()

            val array = suggestionList.optJSONArray("suggestion")
                ?: return emptyList()

            (0 until array.length())
                .map { DrugSuggestion(name = array.getString(it)) }
                .filter { it.name.length <= 40 }          // drop long clinical phrases
                .distinctBy { it.name.lowercase() }       // deduplicate case-insensitively
        } catch (e: Exception) {
            emptyList()
        }
    }
}
