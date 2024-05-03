package com.example.flow.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map

class TokenRepository(
    @ApplicationContext private val context: Context
) {
    private val authTokenKey = stringPreferencesKey("auth_token")

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[authTokenKey] = token
        }
    }

    fun getToken() = context.dataStore.data.map { preferences ->
        preferences[authTokenKey]
    }
}