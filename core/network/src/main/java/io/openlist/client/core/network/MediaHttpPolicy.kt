package io.openlist.client.core.network

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

/** Runs for every network hop, including redirects and adaptive media segments. */
class MediaHttpPolicy(private val baiduSourceHost: String? = null) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!request.url.isHttps) throw InsecureTransportException()
        val baidu = isBaiduMediaHost(request.url) || request.url.host == baiduSourceHost
        return chain.proceed(request.newBuilder()
            // Media URLs are signed; application credentials must never accompany them.
            .removeHeader("Authorization")
            .removeHeader("Cookie")
            .header("User-Agent", if (baidu) "pan.baidu.com" else "OpenList-Android")
            .build())
    }
}

fun isBaiduMediaHost(url: HttpUrl): Boolean = listOf("baidu.com", "baidupcs.com", "baidubce.com").any {
    url.host == it || url.host.endsWith(".$it")
}
