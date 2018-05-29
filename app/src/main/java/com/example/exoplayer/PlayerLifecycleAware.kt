package com.example.exoplayer

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util

class PlayerLifecycleAware(lifecycle: Lifecycle, val context: Context,
                           val onPlayerInitializedListener: (p: SimpleExoPlayer?) -> Unit) : LifecycleObserver {

    private var playWhenReady = true
    private var playbackPosition = 0L
    private var currentWindow = 0
    private var player: SimpleExoPlayer? = null
    private var currentMediaSource: MediaSource? = null

    init {
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun initializePlayerPreKitkat() {
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun releasePlayerPostKitkat() {
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun initializePlayerPostKitkat() {
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun releasePlayerPreKitkat() {
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        if (player == null) {
            val adaptiveVideoTrackSelection = AdaptiveTrackSelection.Factory(BANDWIDTH_METER)

            player = ExoPlayerFactory.newSimpleInstance(DefaultRenderersFactory(context),
                    DefaultTrackSelector(adaptiveVideoTrackSelection),
                    DefaultLoadControl())

            player?.let {
                onPlayerInitializedListener(it)
                it.playWhenReady = playWhenReady
                it.seekTo(currentWindow, playbackPosition)
            }

            currentMediaSource?.let { play(it) }
        }
    }

    private fun releasePlayer() {
        player?.let {
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            playWhenReady = it.playWhenReady
            it.release()
            player = null
        }
    }

    fun buildPlaylist(vararg uris: Uri): MediaSource {
        val dataSourceFactory = DefaultHttpDataSourceFactory("exoplayer-codelab")
        val mediaSources = arrayOfNulls<MediaSource>(uris.size)

        uris.forEachIndexed {
            index, uri ->  mediaSources[index] = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
        }

        return ConcatenatingMediaSource(*mediaSources)
    }

    fun buildDashMediaSource(uri: Uri): MediaSource {
        val manifesDataSourceFactory = DefaultHttpDataSourceFactory("ua")

        val dashChunkSourceFactory = DefaultDashChunkSource.Factory(DefaultHttpDataSourceFactory("ua",
                BANDWIDTH_METER))

        return DashMediaSource.Factory(dashChunkSourceFactory, manifesDataSourceFactory)
                .createMediaSource(uri)
    }

    fun buildHLSMediaSource(uri: Uri): MediaSource {
        val dataSource = DefaultHttpDataSourceFactory("ua")

        val hlsDataSourceFactory = DefaultHlsDataSourceFactory(dataSource)

        return HlsMediaSource.Factory(hlsDataSourceFactory).createMediaSource(uri)
    }

    fun play(mediaSource: MediaSource) {
        currentMediaSource = mediaSource
        player?.prepare(mediaSource, true, false)
    }

    companion object {
        private val BANDWIDTH_METER = DefaultBandwidthMeter()
    }
}