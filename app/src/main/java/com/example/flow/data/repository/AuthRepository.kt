package com.example.flow.data.repository

import android.content.Context
import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.example.flow.MeQuery
import com.example.flow.SignInMutation
import com.example.flow.SignOutMutation
import com.example.flow.SignUpMutation
import com.example.flow.data.model.AuthResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AuthRepository @Inject constructor(
    private val apolloClient: ApolloClient,
    private val tokenRepository: TokenRepository,
    @ApplicationContext private val context: Context
) {
    suspend fun login(username: String, password: String): AuthResult<Unit> {
        return try {
            val token = apolloClient
                .mutation(SignInMutation(username, password))
                .execute()
                .data?.signIn?.token ?: return AuthResult.Unauthorized()

            tokenRepository.saveToken(token)
            AuthResult.Authorized(Unit)
        } catch (e: ApolloException) {
            AuthResult.UnknownError()
        }
    }

    suspend fun signUp(username: String, password: String): AuthResult<Unit> {
        return try {
            val token = apolloClient
                .mutation(SignUpMutation(password, username))
                .execute()
                .data?.signUp?.token ?: return AuthResult.Unauthorized()

            tokenRepository.saveToken(token)
            AuthResult.Authorized(Unit)
        } catch (e: ApolloException) {
            AuthResult.UnknownError()
        }
    }

    suspend fun logout(): AuthResult<Unit> {
        val res = apolloClient.mutation(SignOutMutation()).execute()
        if (res.data?.signOut == true) {
            tokenRepository.saveToken("")
        }
        return AuthResult.Unauthorized()
    }

    suspend fun authenticate(): AuthResult<Unit> {
        return try {
            // If there is no token, throw an exception
            tokenRepository.getToken().first() ?: return AuthResult.Unauthorized()
            // If the token is invalid, throw an exception
            val response = apolloClient.query(MeQuery()).execute().data?.me
            if (response == null) {
                tokenRepository.saveToken("")
                return AuthResult.Unauthorized()
            }
            AuthResult.Authorized(Unit)
        } catch (e: ApolloException) {
            tokenRepository.saveToken("")
            AuthResult.Unauthorized()
        }
    }
}