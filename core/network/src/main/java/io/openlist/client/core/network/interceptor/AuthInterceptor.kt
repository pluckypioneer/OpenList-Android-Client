package io.openlist.client.core.network.interceptor

import io.openlist.client.core.auth.TokenProvider
import io.openlist.client.core.network.InstanceScope
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import io.openlist.client.core.network.InsecureTransportException

/** Credentials belong to an immutable client scope, never the foreground instance. */
class AuthInterceptor(
    private val scope: InstanceScope?,
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (!original.url.isHttps) throw InsecureTransportException()
        val base = scope?.baseUrl?.toHttpUrl()
        val matches = base != null && base.isHttps &&
            original.url.host == base.host && original.url.port == base.port &&
            original.url.encodedPath.startsWith(base.encodedPath.trimEnd('/') + "/")
        // Login validates its newly issued token with meWithToken before persisting it.
        val token = if (matches) original.header("Authorization") ?: scope?.instanceId?.let(tokenProvider::blockingTokenFor) else null
        val request = original.newBuilder().removeHeader("Authorization").apply {
            if (!token.isNullOrBlank()) header("Authorization", token)
        }.build()
        return chain.proceed(request)
    }
}
