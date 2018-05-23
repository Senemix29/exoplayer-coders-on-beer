/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
* limitations under the License.
 */
package com.example.exoplayer

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button

import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashChunkSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util

class PlayerActivity : AppCompatActivity() {
    private var playWhenReady = true
    private var playbackPosition = 0L
    private var currentWindow = 0
    private var player: SimpleExoPlayer? = null

    private lateinit var playerView: SimpleExoPlayerView
    private lateinit var playlistButton: Button
    private lateinit var hlsButton: Button
    private lateinit var dashButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.video_view)
        hlsButton = findViewById(R.id.button_hls)
        dashButton = findViewById(R.id.button_dash)
        playlistButton = findViewById(R.id.button_playlist)

        hlsButton.setOnClickListener{
            play(buildHLSMediaSource(Uri.parse(getString(R.string.media_url_hls))))
        }

        dashButton.setOnClickListener{
            play(buildDashMediaSource(Uri.parse(getString(R.string.media_url_dash))))
        }

        playlistButton.setOnClickListener{
            val playlist = buildPlaylist(Uri.parse(getString(R.string.media_url_mp4)),
                Uri.parse(getString(R.string.media_url_mp3)),
                Uri.parse(getString(R.string.media_url_mp3_surprised)))
            play(playlist)
        }

    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        if (player == null) {
            val adaptiveVideoTrackSelection = AdaptiveTrackSelection.Factory(BANDWIDTH_METER)

            player = ExoPlayerFactory.newSimpleInstance(DefaultRenderersFactory(this),
                    DefaultTrackSelector(adaptiveVideoTrackSelection),
                    DefaultLoadControl())

            playerView.player = player

            player?.playWhenReady = playWhenReady
            player?.seekTo(currentWindow, playbackPosition)
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

    private fun buildPlaylist(vararg uris: Uri): MediaSource {
        val dataSourceFactory = DefaultHttpDataSourceFactory("exoplayer-codelab")
        val mediaSources = arrayOfNulls<MediaSource>(uris.size)

        for (i in uris.indices) {
            mediaSources[i] = ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uris[i])
        }

        return ConcatenatingMediaSource(*mediaSources)
    }

    private fun buildDashMediaSource(uri: Uri): MediaSource {
        val manifesDataSourceFactory = DefaultHttpDataSourceFactory("ua")

        val dashChunkSourceFactory = DefaultDashChunkSource.Factory(DefaultHttpDataSourceFactory("ua",
                BANDWIDTH_METER))

        return DashMediaSource.Factory(dashChunkSourceFactory, manifesDataSourceFactory)
                .createMediaSource(uri)
    }

    private fun buildHLSMediaSource(uri: Uri): MediaSource {
        val dataSource = DefaultHttpDataSourceFactory("ua")

        val hlsDataSourceFactory = DefaultHlsDataSourceFactory(dataSource)

        return HlsMediaSource.Factory(hlsDataSourceFactory).createMediaSource(uri)
    }

    private fun play(mediaSource: MediaSource) {
        player?.prepare(mediaSource, true, false)
    }

    companion object {

        private val BANDWIDTH_METER = DefaultBandwidthMeter()
    }
}
