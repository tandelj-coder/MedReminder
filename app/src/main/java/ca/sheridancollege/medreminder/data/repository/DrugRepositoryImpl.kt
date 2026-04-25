package ca.sheridancollege.medreminder.data.repository

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
class DrugRepositoryImpl @Inject constructor() : DrugRepository {

    override suspend fun searchDrugs(query: String): Result<List<DrugSuggestion>> =
        withContext(Dispatchers.IO) {
            try {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val url = URL("https://rxnav.nlm.nih.gov/REST/spellingsuggestions.json?name=$encoded")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5_000
                connection.readTimeout = 5_000
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext Result.failure(
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
