package com.chargercontrol.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class Prefs(private val context: Context) {
    companion object {
        val LIMIT_KEY = intPreferencesKey("limit_percent")
        val ENABLED_KEY = booleanPreferencesKey("service_enabled")
    }

    val limitFlow: Flow<Int> = context.dataStore.data.map { it[LIMIT_KEY] ?: 80 }
    val enabledFlow: Flow<Boolean> = context.dataStore.data.map { it[ENABLED_KEY] ?: false }

    suspend fun setLimit(value: Int) {
        context.dataStore.edit { it[LIMIT_KEY] = value }
    }

    suspend fun setEnabled(value: Boolean) {
        context.dataStore.edit { it[ENABLED_KEY] = value }
    }
}
