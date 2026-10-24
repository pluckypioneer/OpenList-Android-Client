@file:OptIn(UnstableApi::class) // Media3's ExoPlayer surface is broadly @UnstableApi-marked

package io.openlist.client.feature.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Surface
import coil.compose.AsyncImage
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import io.openlist.client.core.designsystem.Spacing
import io.openlist.client.core.model.MediaSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Audio layout for [MediaPlayerScreen] (v0.4_EXECUTION_PLAN.md §11 S5-T3):
 * file name/path, a play/pause button, a scrubbable progress slider, and
 * current/total time text -- no lyrics (explicitly out of scope per P-411).
 *
 * Shares the exact same [rememberOpenListExoPlayer] player-creation/release/
 * error/position-sampling logic as [MediaPlayerSurface] (this Sprint's
 * mandatory DoD: "no duplicate ExoPlayer init code") -- the only things this
 * composable adds are audio-specific: this hand-rolled transport UI (no
 * [androidx.media3.ui.PlayerView], since there is no video surface to show)
 * and its own local polling of `exoPlayer.currentPosition`/`duration` to
 * drive the slider's live position (independent of, and more frequent than,
 * [rememberOpenListExoPlayer]'s coarser 1s ViewModel-position-sample cadence
 * -- that one exists for seek-back-after-refresh, this one is for a
 * responsive-looking slider).
 */
@Composable
internal fun AudioPlayerSurface(
    mediaSource: MediaSource,
    scopedHeaders: Map<String, String>,
    initialPositionMs: Long,
    onPositionSample: (Long) -> Unit,
    onHttpClientError: suspend () -> MediaSource?,
    onTerminalError: (PlaybackException) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val exoPlayer = rememberOpenListExoPlayer(
        mediaSource = mediaSource,
        scopedHeaders = scopedHeaders,
        initialPositionMs = initialPositionMs,
        onPositionSample = onPositionSample,
        onError = { error ->
            if (MediaPlaybackErrorClassifier.isRetryableHttpClientError(error)) {
                scope.launch { onHttpClientError() }
            } else {
                onTerminalError(error)
            }
        },
    )

    var isPlaying by remember(exoPlayer) { mutableStateOf(exoPlayer.isPlaying) }
    var playWhenReady by remember(exoPlayer) { mutableStateOf(exoPlayer.playWhenReady) }
    var currentPositionMs by remember(exoPlayer) { mutableLongStateOf(exoPlayer.currentPosition) }
    var durationMs by remember(exoPlayer) { mutableLongStateOf(exoPlayer.duration.coerceAtLeast(0L)) }
    var sliderDragValue by remember(exoPlayer) { mutableFloatStateOf(-1f) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onEvents(player: Player, events: Player.Events) {
                durationMs = player.duration.coerceAtLeast(0L)
                playWhenReady = player.playWhenReady && player.playbackState != Player.STATE_ENDED
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    // Faster local polling than the 1s ViewModel-position-sample cadence in
    // rememberOpenListExoPlayer -- purely cosmetic (a visibly moving
    // slider), not persisted anywhere.
    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(250L)
            if (sliderDragValue < 0f) {
                currentPositionMs = exoPlayer.currentPosition
            }
            durationMs = exoPlayer.duration.coerceAtLeast(0L)
        }
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("正在播放", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Surface(
            modifier = Modifier.widthIn(max = 300.dp).fillMaxWidth().aspectRatio(1f),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.tertiaryContainer,
        ) {
            if (mediaSource.artworkUrl != null) {
                AsyncImage(
                    model = mediaSource.artworkUrl,
                    contentDescription = "专辑封面",
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.size(210.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier.size(150.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.MusicNote, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(mediaSource.title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text(
                mediaSource.path.substringBeforeLast('/').ifBlank { "音乐" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(Modifier.widthIn(max = 480.dp).fillMaxWidth()) {
            val sliderPositionMs = if (sliderDragValue >= 0f) (sliderDragValue * durationMs).toLong() else currentPositionMs
            Slider(
                value = if (durationMs > 0) (sliderPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f,
                enabled = durationMs > 0 && exoPlayer.isCurrentMediaItemSeekable,
                onValueChange = { sliderDragValue = it },
                onValueChangeFinished = {
                    if (sliderDragValue >= 0f && durationMs > 0) {
                        currentPositionMs = (sliderDragValue * durationMs).toLong()
                        exoPlayer.seekTo(currentPositionMs)
                    }
                    sliderDragValue = -1f
                },
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDurationMs(sliderPositionMs), style = MaterialTheme.typography.labelMedium)
                Text(formatDurationMs(durationMs), style = MaterialTheme.typography.labelMedium)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xl), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                enabled = exoPlayer.isCurrentMediaItemSeekable,
                onClick = { exoPlayer.seekTo((exoPlayer.currentPosition - 10_000).coerceAtLeast(0)) },
                modifier = Modifier.size(56.dp),
            ) { Icon(Icons.Filled.Replay10, "后退 10 秒", Modifier.size(28.dp)) }
            FilledIconButton(
                onClick = {
                    if (playWhenReady) exoPlayer.pause() else {
                        if (exoPlayer.playbackState == Player.STATE_ENDED) exoPlayer.seekTo(0)
                        exoPlayer.play()
                    }
                },
                modifier = Modifier.size(80.dp),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Icon(if (isPlaying || playWhenReady) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    if (isPlaying || playWhenReady) "暂停" else "播放", Modifier.size(40.dp))
            }
            IconButton(
                enabled = durationMs > 0 && exoPlayer.isCurrentMediaItemSeekable,
                onClick = { exoPlayer.seekTo((exoPlayer.currentPosition + 10_000).coerceAtMost(durationMs)) },
                modifier = Modifier.size(56.dp),
            ) { Icon(Icons.Filled.Forward10, "前进 10 秒", Modifier.size(28.dp)) }
        }
    }
}
private fun formatDurationMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
