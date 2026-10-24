package io.openlist.client.core.network

import io.openlist.client.core.common.SafeLogger
import io.openlist.client.core.database.AppPreferences
import io.openlist.client.core.network.interceptor.AuthInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/** Caches clients by immutable instance ID and base URL. Null ID means anonymous.
 * Logging omits bodies and headers; authenticated API redirects are disabled. */
@Singleton
class OpenListClientFactory @Inject constructor(
    private val tokenProvider: io.openlist.client.core.auth.TokenProvider,
    private val json: Json,
    appPreferences: AppPreferences,
) {
    private val apiCache = ConcurrentHashMap<Pair<String, String?>, OpenListApi>()
    private val loggingEnabled = AtomicBoolean(false)
    private val loggingInterceptor = HttpLoggingInterceptor { message -> SafeLogger.d("OkHttp", message) }

    init {
        appPreferences.loggingEnabled
            .onEach { enabled -> loggingEnabled.set(enabled) }
            .launchIn(CoroutineScope(SupervisorJob() + Dispatchers.Default))
    }

    fun apiFor(baseUrl: String, instanceId: String? = null): OpenListApi {
        val normalized = ensureTrailingSlash(baseUrl)
        return apiCache.getOrPut(normalized to instanceId) { buildApi(normalized, instanceId) }
    }

    private fun buildApi(baseUrl: String, instanceId: String?): OpenListApi {
        val dynamicLogging = Interceptor { chain ->
            loggingInterceptor.level =
                if (loggingEnabled.get()) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            loggingInterceptor.intercept(chain)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(instanceId?.let { InstanceScope(it, baseUrl) }, tokenProvider))
            .followRedirects(false)
            .followSslRedirects(false)
            .addInterceptor(dynamicLogging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(KotlinxJsonConverterFactory(json, contentType))
            .build()
            .create()
    }

    private fun ensureTrailingSlash(baseUrl: String): String =
        if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
}
