package com.trulyao.bookie.lib

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app")

enum class StoreKey {
    User
}

// A simple wrapper class to get and set (insert, update) data in the DataStore
class Store {

    companion object {
        // Get preference key and cast to generic type, unsafe but we are hoping the generic will constraint the input anyway
        private fun <T> getPrefKey(key: StoreKey): Preferences.Key<T> {
            return when (key) {
                StoreKey.User -> intPreferencesKey("signed_in_user")
            } as Preferences.Key<T>
        }

        // Get data from the datastore or return a default
        public fun <T> getOrDefault(context: Context, key: StoreKey, default: T): Flow<T> {
            val prefKey = getPrefKey<T>(key)
            return context.dataStore.data.map { data ->
                data[prefKey] ?: default
            }
        }

        // Insert or update a key in the datastore
        public suspend fun <T> set(context: Context, key: StoreKey, value: T) {
            val prefKey = getPrefKey<T>(key)
            context.dataStore.edit { data ->
                data[prefKey] = value
            }
        }
    }
}
