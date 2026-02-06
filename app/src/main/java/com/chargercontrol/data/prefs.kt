package com.chargercontrol.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class Prefs(private val context: Context) {
    private val ENABLED_KEY = booleanPreferencesKey("enabled")
    private val LIMIT_KEY = intPreferencesKey("limit")

    val enabledFlow: Flow<Boolean> = context.dataStore.data.map { it[ENABLED_KEY] ?: false }
    val limitFlow: Flow<Int> = context.dataStore.data.map { it[LIMIT_KEY] ?: 80 }

    suspend fun setEnabled(value: Boolean) {
        context.dataStore.edit { it[ENABLED_KEY] = value }
    }

    suspend fun setLimit(value: Int) {
        context.dataStore.edit { it[LIMIT_KEY] = value }
    }
}
