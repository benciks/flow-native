package com.example.flow.di

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.network.okHttpClient
import com.example.flow.BuildConfig
import com.example.flow.data.repository.TokenRepository
import com.example.flow.ui.screens.destinations.LoginScreenDestination
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

object ApiUrls {
    const val DEV_BASE_URL = "http://10.0.2.2:3000/query/"
    const val PROD_BASE_URL = "http://flow.benciks.me/query"
}

fun getBaseUrl(): String {
    return if (BuildConfig.DEBUG) {
        ApiUrls.DEV_BASE_URL
    } else {
        ApiUrls.PROD_BASE_URL
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ApolloModule {

    private val cacheFactory = MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)

    @Provides
    @Singleton
    fun provideApolloClient(
        tokenRepository: TokenRepository
    ): ApolloClient {
        val authInterceptor = AuthorizationInterceptor(tokenRepository)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return ApolloClient.Builder()
            .serverUrl("http://10.0.2.2:3000/query")
            .normalizedCache(cacheFactory)
            .fetchPolicy(FetchPolicy.NetworkFirst)
            .okHttpClient(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideTokenRepository(
        @ApplicationContext context: Context
    ): TokenRepository {
        return TokenRepository(context)
    }

    class AuthorizationInterceptor(
        private val tokenRepository: TokenRepository,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val token = runBlocking {
                tokenRepository.getToken().firstOrNull() ?: ""
            }
            val request = chain.request().newBuilder()
                .apply {
                    addHeader("Authorization", token)
                }
                .build()

            return chain.proceed(request)
        }
    }
}