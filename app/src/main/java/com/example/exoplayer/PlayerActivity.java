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
package com.example.exoplayer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;
    private Button playlistButton;
    private Button hlsButton;
    private Button dashButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playerView = findViewById(R.id.video_view);
        hlsButton = findViewById(R.id.button_hls);
        dashButton = findViewById(R.id.button_dash);
        playlistButton = findViewById(R.id.button_playlist);

        hlsButton.setOnClickListener(this);
        dashButton.setOnClickListener(this);
        playlistButton.setOnClickListener(this);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void initializePlayer() {
        if (player == null) {
            TrackSelection.Factory adaptiveVideoTrackSelection = new AdaptiveTrackSelection
                    .Factory(BANDWIDTH_METER);

            player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this),
                    new DefaultTrackSelector(adaptiveVideoTrackSelection),
                    new DefaultLoadControl());

            playerView.setPlayer(player);

            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    private MediaSource buildPlaylist(Uri... uris) {
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory
                ("exoplayer-codelab");
        MediaSource[] mediaSources = new MediaSource[uris.length];

        for (int i = 0; i < uris.length; i++) {
            mediaSources[i] = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uris[i]);
        }

        return new ConcatenatingMediaSource(mediaSources);
    }

    private MediaSource buildDashMediaSource(Uri uri) {
        DataSource.Factory manifesDataSourceFactory = new DefaultHttpDataSourceFactory("ua");

        DashChunkSource.Factory dashChunkSourceFactory =
                new DefaultDashChunkSource.Factory(new DefaultHttpDataSourceFactory("ua",
                        BANDWIDTH_METER));

        return new DashMediaSource.Factory(dashChunkSourceFactory, manifesDataSourceFactory)
                .createMediaSource(uri);
    }

    private MediaSource buildHLSMediaSource(Uri uri) {
        DataSource.Factory dataSource = new DefaultHttpDataSourceFactory("ua");

        HlsDataSourceFactory hlsDataSourceFactory = new DefaultHlsDataSourceFactory(dataSource);

        return new HlsMediaSource.Factory(hlsDataSourceFactory).createMediaSource(uri);
    }

    private void play(MediaSource mediaSource) {
        player.prepare(mediaSource, true, false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_playlist:
                MediaSource playlist =
                        buildPlaylist(Uri.parse(getString(R.string.media_url_mp4)),
                                Uri.parse(getString(R.string.media_url_mp3)),
                                Uri.parse(getString(R.string.media_url_mp3_surprised)));
                play(playlist);
                break;
            case R.id.button_hls:
                play(buildHLSMediaSource(Uri.parse(getString(R.string.media_url_hls))));
                break;
            case R.id.button_dash:
                play(buildDashMediaSource(Uri.parse(getString(R.string.media_url_dash))));
                break;
        }
    }
}
