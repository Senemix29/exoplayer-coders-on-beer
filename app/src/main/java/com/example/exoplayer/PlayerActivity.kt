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

import android.net.Uri.parse
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.SimpleExoPlayerView

class PlayerActivity : AppCompatActivity() {

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

        val playerLifecycleAware = PlayerLifecycleAware(lifecycle, applicationContext) {
            initializedPlayer: SimpleExoPlayer? -> playerView.player = initializedPlayer
        }

        hlsButton.setOnClickListener {
            with(playerLifecycleAware) {
                play(buildHLSMediaSource(parse(getString(R.string.media_url_hls))))
            }
        }

        dashButton.setOnClickListener {
            with(playerLifecycleAware) {
                play(buildDashMediaSource(parse(getString(R.string.media_url_dash))))
            }
        }

        playlistButton.setOnClickListener {
            with(playerLifecycleAware) {
                val playlist = buildPlaylist(parse(getString(R.string.media_url_mp4)),
                        parse(getString(R.string.media_url_mp3)),
                        parse(getString(R.string.media_url_mp3_surprised)))
                play(playlist)
            }
        }

    }
}
