package com.chargercontrol.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class Prefs(private val context: Context) {
    private val LIMIT_KEY = intPreferencesKey("charge_limit")
    private val ENABLED_KEY = booleanPreferencesKey("control_enabled")

    val limitFlow: Flow<Int> = context.dataStore.data.map { it[LIMIT_KEY] ?: 80 }
    val enabledFlow: Flow<Boolean> = context.dataStore.data.map { it[ENABLED_KEY] ?: false }

    suspend fun saveLimit(limit: Int) {
        context.dataStore.edit { it[LIMIT_KEY] = limit }
    }

    suspend fun saveEnabled(enabled: Boolean) {
        context.dataStore.edit { it[ENABLED_KEY] = enabled }
    }
}
