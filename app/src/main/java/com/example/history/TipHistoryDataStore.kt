package com.example.history

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.io.IOException

// Application context extension property for the singleton tip history DataStore instance
private val Context.tipDataStore: DataStore<Preferences> by preferencesDataStore(name = "tip_history_preferences")

/**
 * =========================================================================================
 * FEATURE: Save Prompt Behavior Modes (SavePromptMode)
 * =========================================================================================
 *
 * Description:
 * Defines user preferences for the "Do you want to save it?" modal prompt dialog.
 * - [ALWAYS]: Prompts the user each time a bill calculation is performed.
 * - [NEVER]: Suppresses automatic prompt dialogs; calculations are manually saved via action button.
 * =========================================================================================
 */
enum class SavePromptMode(val value: String) {
    ALWAYS("ALWAYS"),
    NEVER("NEVER");

    companion object {
        /**
         * Resolves a [SavePromptMode] from a persisted string key.
         *
         * @param value Persisted string value ("ALWAYS" or "NEVER").
         * @return Matching [SavePromptMode], defaulting to [ALWAYS] if null or unrecognized.
         */
        fun fromString(value: String?): SavePromptMode {
            return if (value.equals("NEVER", ignoreCase = true)) NEVER else ALWAYS
        }
    }
}

/**
 * =========================================================================================
 * FEATURE: DataStore Tip History & Preferences Repository (TipHistoryDataStore)
 * =========================================================================================
 *
 * Description:
 * Manages persistent storage of past tip calculation transactions and user prompt preferences
 * using Jetpack DataStore Preferences. Ensures asynchronous, non-blocking I/O operations and
 * automatic thread safety.
 *
 * Characteristics:
 * - Reactive [Flow] streams: Emits updates whenever history or preferences change.
 * - Capacity bound: Limits history list to [MAX_HISTORY_ITEMS] (50 items) to conserve storage.
 * - Resilient exception handling: Catches [IOException] during preference file reads.
 *
 * Dependencies:
 * - [androidx.datastore.preferences.core.DataStore]: Underlying asynchronous key-value engine.
 * - [TipTransaction]: Serialization and deserialization entity.
 * =========================================================================================
 */
class TipHistoryDataStore(private val context: Context) {

    private val dataStore = context.applicationContext.tipDataStore

    companion object {
        private val KEY_HISTORY_JSON = stringPreferencesKey("key_recent_tips_json")
        private val KEY_SAVE_PROMPT_MODE = stringPreferencesKey("key_save_prompt_mode")
        private val KEY_TUTORIAL_MODE = stringPreferencesKey("key_tutorial_mode")
        private const val MAX_HISTORY_ITEMS = 50
    }

    /**
     * Observable [Flow] of saved tip transactions, ordered chronologically newest first.
     * Emits an empty list if no transactions exist or on read error.
     */
    val historyFlow: Flow<List<TipTransaction>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val jsonString = preferences[KEY_HISTORY_JSON] ?: "[]"
            parseHistoryJson(jsonString)
        }

    /**
     * Observable [Flow] of the user's preference for the "Do you want to save it?" modal mask ([SavePromptMode.ALWAYS] vs [SavePromptMode.NEVER]).
     */
    val savePromptModeFlow: Flow<SavePromptMode> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            SavePromptMode.fromString(preferences[KEY_SAVE_PROMPT_MODE])
        }

    /**
     * Observable [Flow] indicating whether the onboarding tutorial tour should launch automatically ("ALWAYS" vs "NEVER").
     */
    val tutorialModeFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_TUTORIAL_MODE] ?: "NEVER"
        }

    /**
     * Persists a newly calculated [TipTransaction] into the history list.
     * Inserts the new entry at index 0 and prunes items exceeding [MAX_HISTORY_ITEMS].
     *
     * @param transaction The [TipTransaction] to save.
     */
    suspend fun saveTransaction(transaction: TipTransaction) {
        dataStore.edit { preferences ->
            val currentJson = preferences[KEY_HISTORY_JSON] ?: "[]"
            val currentList = parseHistoryJson(currentJson).toMutableList()
            // Add new transaction at the beginning (index 0)
            currentList.add(0, transaction)
            // Trim to max capacity
            val trimmedList = currentList.take(MAX_HISTORY_ITEMS)
            preferences[KEY_HISTORY_JSON] = serializeHistoryJson(trimmedList)
        }
    }

    /**
     * Removes an individual transaction from history by its unique identifier.
     *
     * @param id Unique UUID string of the transaction to delete.
     */
    suspend fun deleteTransaction(id: String) {
        dataStore.edit { preferences ->
            val currentJson = preferences[KEY_HISTORY_JSON] ?: "[]"
            val updatedList = parseHistoryJson(currentJson).filter { it.id != id }
            preferences[KEY_HISTORY_JSON] = serializeHistoryJson(updatedList)
        }
    }

    /**
     * Clears all saved tip history transactions from persistent storage.
     */
    suspend fun clearAllHistory() {
        dataStore.edit { preferences ->
            preferences[KEY_HISTORY_JSON] = "[]"
        }
    }

    /**
     * Updates and persists the user's preferred save prompt behavior mode.
     *
     * @param mode Target [SavePromptMode] ([SavePromptMode.ALWAYS] or [SavePromptMode.NEVER]).
     */
    suspend fun setSavePromptMode(mode: SavePromptMode) {
        dataStore.edit { preferences ->
            preferences[KEY_SAVE_PROMPT_MODE] = mode.value
        }
    }

    /**
     * Updates and persists the onboarding tutorial display policy.
     *
     * @param mode Policy string ("ALWAYS" or "NEVER").
     */
    suspend fun setTutorialMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_TUTORIAL_MODE] = mode
        }
    }

    /**
     * Helper to deserialize a JSON array string into a typed list of [TipTransaction] objects.
     *
     * @param jsonString Raw JSON string representation of transaction array.
     * @return List of parsed transactions, or empty list if string is malformed.
     */
    private fun parseHistoryJson(jsonString: String): List<TipTransaction> {
        return try {
            val array = JSONArray(jsonString)
            val list = mutableListOf<TipTransaction>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val item = TipTransaction.fromJson(obj)
                if (item != null) {
                    list.add(item)
                }
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Helper to serialize a list of [TipTransaction] objects into a JSON array string.
     *
     * @param list List of [TipTransaction] instances.
     * @return Formatted JSON array string.
     */
    private fun serializeHistoryJson(list: List<TipTransaction>): String {
        val array = JSONArray()
        list.forEach { transaction ->
            array.put(transaction.toJson())
        }
        return array.toString()
    }
}
