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
import com.example.flow.ModifyTimewHookMutation
import com.example.flow.SignInMutation
import com.example.flow.SignOutMutation
import com.example.flow.SignUpMutation
import com.example.flow.data.model.AuthResult
import com.example.flow.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.example.flow.data.mapper.toUser

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AuthRepository @Inject constructor(
    private val apolloClient: ApolloClient,
    private val tokenRepository: TokenRepository,
    @ApplicationContext private val context: Context
) {
    suspend fun login(username: String, password: String): AuthResult<User> {
        return try {
            val signIn = apolloClient
                .mutation(SignInMutation(username, password))
                .execute()
                .data?.signIn ?: return AuthResult.Unauthorized()

            val token = signIn.token
            val user = signIn.toUser()

            tokenRepository.saveToken(token)
            AuthResult.Authorized(user)
        } catch (e: ApolloException) {
            AuthResult.UnknownError()
        }
    }

    suspend fun signUp(username: String, password: String): AuthResult<User> {
        return try {
            val signUp = apolloClient
                .mutation(SignUpMutation(password, username))
                .execute()
                .data?.signUp ?: return AuthResult.Unauthorized()

            val token = signUp.token
            val user = signUp.toUser()

            tokenRepository.saveToken(token)
            AuthResult.Authorized(user)
        } catch (e: ApolloException) {
            AuthResult.UnknownError()
        }
    }

    suspend fun logout(): AuthResult<User> {
        val res = apolloClient.mutation(SignOutMutation()).execute()
        if (res.data?.signOut == true) {
            tokenRepository.saveToken("")
        }
        return AuthResult.Unauthorized()
    }

    suspend fun authenticate(): AuthResult<User> {
        return try {
            // If there is no token, throw an exception
            tokenRepository.getToken().first() ?: return AuthResult.Unauthorized()
            // If the token is invalid, throw an exception
            val response = apolloClient.query(MeQuery()).execute().data?.me

            if (response == null) {
                tokenRepository.saveToken("")
                return AuthResult.Unauthorized()
            }

            val user = response.toUser()
            AuthResult.Authorized(user)
        } catch (e: ApolloException) {
            tokenRepository.saveToken("")
            AuthResult.Unauthorized()
        }
    }

    suspend fun setTimewHook(enabled: Boolean): Boolean {
        return try {
            val res = apolloClient.mutation(ModifyTimewHookMutation(enabled)).execute()
            return res.data?.setTimewHook == true
        } catch (e: ApolloException) {
            return false
        }
    }
}