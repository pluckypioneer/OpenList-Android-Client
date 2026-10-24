package io.openlist.client.feature.preview

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.openlist.client.core.model.PreviewSource
import io.openlist.client.core.network.MediaHttpPolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

private const val MAX_PDF_BYTES = 100L * 1024 * 1024
private val pdfClient = OkHttpClient.Builder()
    .addNetworkInterceptor(MediaHttpPolicy())
    .followSslRedirects(false)
    .callTimeout(90, TimeUnit.SECONDS)
    .build()

/** One bounded bitmap at a time; no online viewer receives document URLs. */
@Composable
internal fun PdfPreviewSurface(
    source: PreviewSource.RemoteUrl?,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val url = source?.url
    var attempt by remember { mutableIntStateOf(0) }
    var document by remember(url) { mutableStateOf<File?>(null) }
    var pageCount by remember(url) { mutableIntStateOf(0) }
    var pageIndex by rememberSaveable(url) { mutableIntStateOf(0) }
    var error by remember(url) { mutableStateOf<String?>(null) }
    var bitmap by remember(url) { mutableStateOf<Bitmap?>(null) }
    var scale by remember(url, pageIndex) { mutableFloatStateOf(1f) }
    var offset by remember(url, pageIndex) { mutableStateOf(Offset.Zero) }

    LaunchedEffect(url, attempt) {
        document = null
        bitmap = null
        error = null
        var temporary: File? = null
        try {
            if (url == null) throw IOException("PDF 地址不可用")
            withContext(Dispatchers.IO) {
                val directory = File(context.cacheDir, "pdf-preview").apply { mkdirs() }
                val file = File.createTempFile("document-", ".pdf", directory)
                temporary = file
                pdfClient.newCall(Request.Builder().url(url).build()).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("下载失败（HTTP ${response.code}），请返回后重新打开")
                    val body = response.body ?: throw IOException("服务器未返回 PDF 内容")
                    if (body.contentLength() > MAX_PDF_BYTES) throw IOException("PDF 超过 100 MB，请下载后查看")
                    body.byteStream().use { input ->
                        file.outputStream().use { output ->
                            val buffer = ByteArray(32 * 1024)
                            var total = 0L
                            while (true) {
                                ensureActive()
                                val count = input.read(buffer)
                                if (count < 0) break
                                total += count
                                if (total > MAX_PDF_BYTES) throw IOException("PDF 超过 100 MB，请下载后查看")
                                output.write(buffer, 0, count)
                            }
                        }
                    }
                }
            }
            val file = checkNotNull(temporary)
            pageCount = withContext(Dispatchers.IO) { withPdf(file) { it.pageCount } }
            if (pageCount == 0) throw IOException("PDF 中没有可显示的页面")
            pageIndex = pageIndex.coerceIn(0, pageCount - 1)
            document = file
            awaitCancellation()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            error = if (failure is SecurityException) "此 PDF 已加密或不受支持，请下载后使用其他应用打开" else
                if (failure is IOException) failure.message ?: "PDF 加载失败" else "无法打开此 PDF，请下载后查看"
        } finally {
            withContext(NonCancellable + Dispatchers.IO) { temporary?.let { synchronized(it) { it.delete() } } }
        }
    }

    LaunchedEffect(document, pageIndex) {
        bitmap = null
        val file = document ?: return@LaunchedEffect
        try {
            bitmap = withContext(Dispatchers.IO) {
                withPdf(file) { renderer ->
                    renderer.openPage(pageIndex).use { page ->
                        val ratio = page.height.toDouble() / page.width.coerceAtLeast(1)
                        val width = minOf(1600.0, sqrt(4_000_000.0 / ratio)).toInt().coerceAtLeast(1)
                        val height = (width * ratio).toInt().coerceIn(1, 4_000_000 / width)
                        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
                            it.eraseColor(android.graphics.Color.WHITE)
                            page.render(it, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        }
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            error = "页面渲染失败，请重试或下载后查看"
        }
    }

    Column(modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth().clipToBounds(), contentAlignment = Alignment.Center) {
            val currentBitmap = bitmap
            when {
                error != null -> Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = { attempt++ }) { Text("重试") }
                    FilledTonalButton(onClick = onDownload) { Text("下载 PDF") }
                }
                currentBitmap == null -> CircularProgressIndicator()
                else -> Image(
                    bitmap = currentBitmap.asImageBitmap(),
                    contentDescription = "第 ${pageIndex + 1} 页，共 $pageCount 页",
                    modifier = Modifier.fillMaxSize()
                        .transformable(rememberTransformableState { zoom, pan, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            offset = if (scale == 1f) Offset.Zero else offset + pan
                        })
                        .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y),
                )
            }
        }
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = { pageIndex-- }, enabled = document != null && pageIndex > 0) { Text("上一页") }
                    Text(if (pageCount > 0) "${pageIndex + 1} / $pageCount" else "PDF")
                    TextButton(onClick = { pageIndex++ }, enabled = document != null && pageIndex + 1 < pageCount) { Text("下一页") }
                }
                Row {
                    TextButton(onClick = { scale = (scale + 0.5f).coerceAtMost(4f) }, enabled = bitmap != null) { Text("放大") }
                    TextButton(onClick = { scale = 1f; offset = Offset.Zero }) { Text("适合页面") }
                    TextButton(onClick = onDownload) { Text("下载") }
                }
            }
        }
    }
}

private inline fun <T> withPdf(file: File, block: (PdfRenderer) -> T): T = synchronized(file) {
    val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = try { PdfRenderer(descriptor) } catch (failure: Exception) {
        descriptor.close()
        throw failure
    }
    renderer.use(block)
}
