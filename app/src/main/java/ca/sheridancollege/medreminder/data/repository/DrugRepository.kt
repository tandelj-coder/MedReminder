package ca.sheridancollege.medreminder.data.repository

import ca.sheridancollege.medreminder.domain.model.DrugSuggestion

interface DrugRepository {
    suspend fun searchDrugs(query: String): Result<List<DrugSuggestion>>
}
