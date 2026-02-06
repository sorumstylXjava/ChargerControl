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
    private val THERMAL_KEY = booleanPreferencesKey("thermal_cutoff")
    private val MAX_TEMP_KEY = floatPreferencesKey("max_temp")
    private val POWER_SAVE_KEY = booleanPreferencesKey("power_save")

    val enabledFlow: Flow<Boolean> = context.dataStore.data.map { it[ENABLED_KEY] ?: false }
    val limitFlow: Flow<Int> = context.dataStore.data.map { it[LIMIT_KEY] ?: 100 }
    val thermalFlow: Flow<Boolean> = context.dataStore.data.map { it[THERMAL_KEY] ?: false }
    val maxTempFlow: Flow<Float> = context.dataStore.data.map { it[MAX_TEMP_KEY] ?: 40f }
    val powerSaveFlow: Flow<Boolean> = context.dataStore.data.map { it[POWER_SAVE_KEY] ?: false }

    suspend fun setEnabled(value: Boolean) { context.dataStore.edit { it[ENABLED_KEY] = value } }
    suspend fun setLimit(value: Int) { context.dataStore.edit { it[LIMIT_KEY] = value } }
    suspend fun setThermal(value: Boolean) { context.dataStore.edit { it[THERMAL_KEY] = value } }
    suspend fun setMaxTemp(value: Float) { context.dataStore.edit { it[MAX_TEMP_KEY] = value } }
    suspend fun setPowerSave(value: Boolean) { context.dataStore.edit { it[POWER_SAVE_KEY] = value } }
}
