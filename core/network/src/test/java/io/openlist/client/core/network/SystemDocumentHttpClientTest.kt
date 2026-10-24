package io.openlist.client.core.network

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.Protocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemDocumentHttpClientTest {
    private val client = SystemDocumentHttpClient()

    @Test
    fun `range call sends exact range and accepts matching 206 without credentials over HTTP`() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(206).setHeader("Content-Range", "bytes 3-6/10").setBody("3456"))
            server.start()
            client.newRangeCall(server.url("/file").toString(), 3, 4, server.url("/").toString(), "Bearer secret").execute().use { response ->
                assertTrue(client.isTrustedRangeResponse(response, 3, 4, 10))
            }
            val request = requireNotNull(server.takeRequest())
            assertEquals("bytes=3-6", request.getHeader("Range"))
            // MockWebServer uses cleartext HTTP. Credentials are HTTPS-only,
            // even when the request and instance origins otherwise match.
            assertNull(request.getHeader("Authorization"))
        }
    }

    @Test
    fun `system document transport is pinned to HTTP 1 1 without changing request security`() {
        assertEquals(listOf(Protocol.HTTP_1_1), client.client.protocols)
    }

    @Test
    fun `200 or malformed Content-Range is never trusted`() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(200).setBody("whole-file"))
            server.enqueue(MockResponse().setResponseCode(206).setHeader("Content-Range", "bytes 0-3/10").setBody("0123"))
            server.start()
            client.newRangeCall(server.url("/file").toString(), 3, 4, server.url("/").toString(), null).execute().use { response ->
                assertFalse(client.isTrustedRangeResponse(response, 3, 4, 10))
            }
            client.newRangeCall(server.url("/file").toString(), 3, 4, server.url("/").toString(), null).execute().use { response ->
                assertFalse(client.isTrustedRangeResponse(response, 3, 4, 10))
            }
        }
    }
}
