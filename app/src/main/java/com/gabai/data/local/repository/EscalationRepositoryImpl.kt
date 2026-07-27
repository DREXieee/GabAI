package com.gabai.data.local.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gabai.domain.repository.EscalationConfirmation
import com.gabai.domain.repository.EscalationEvent
import com.gabai.domain.repository.EscalationRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.escalationDataStore by preferencesDataStore(name = "escalation_prefs")
private val ESCALATION_LOG_KEY = stringPreferencesKey("escalation_log_json")

// No real backend/staffing system exists yet - placeholder SLA shown to the user.
private const val DEFAULT_RESPONSE_MINUTES = 15

class EscalationRepositoryImpl(private val context: Context) : EscalationRepository {

    private val gson = Gson()
    private val listType = object : TypeToken<List<EscalationEvent>>() {}.type

    override suspend fun logEscalationEvent(event: EscalationEvent) {
        context.escalationDataStore.edit { prefs ->
            val current: List<EscalationEvent> = prefs[ESCALATION_LOG_KEY]?.let {
                gson.fromJson(it, listType)
            } ?: emptyList()
            prefs[ESCALATION_LOG_KEY] = gson.toJson(current + event)
        }
    }

    override suspend fun requestHumanSupport(event: EscalationEvent): EscalationConfirmation {
        logEscalationEvent(event)
        return EscalationConfirmation(estimatedResponseMinutes = DEFAULT_RESPONSE_MINUTES, requestId = event.id)
    }

    override fun observeEscalationHistory(): Flow<List<EscalationEvent>> =
        context.escalationDataStore.data.map { prefs ->
            prefs[ESCALATION_LOG_KEY]?.let { gson.fromJson<List<EscalationEvent>>(it, listType) } ?: emptyList()
        }
}
