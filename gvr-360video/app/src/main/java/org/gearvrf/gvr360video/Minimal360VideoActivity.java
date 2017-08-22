/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.gvr360video;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;

import java.io.IOException;

public class Minimal360VideoActivity extends GVRActivity {

    private static final String TAG = "MainActivity";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //check permissions before doing anything


        if (!USE_EXO_PLAYER) {
            videoSceneObjectPlayer = makeMediaPlayer();
        } else {
            videoSceneObjectPlayer = makeExoPlayer();
        }

        if (null != videoSceneObjectPlayer) {
            final Minimal360Video main = new Minimal360Video(videoSceneObjectPlayer);
            setMain(main);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != videoSceneObjectPlayer) {
            final Object player = videoSceneObjectPlayer.getPlayer();
            if (!USE_EXO_PLAYER) {
                MediaPlayer mediaPlayer = (MediaPlayer) player;
                mediaPlayer.pause();
            } else {
                ExoPlayer exoPlayer = (ExoPlayer) player;
                exoPlayer.setPlayWhenReady(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != videoSceneObjectPlayer) {
            final Object player = videoSceneObjectPlayer.getPlayer();
            if (!USE_EXO_PLAYER) {
                MediaPlayer mediaPlayer = (MediaPlayer) player;
                mediaPlayer.start();
            } else {
                ExoPlayer exoPlayer = (ExoPlayer) player;
                exoPlayer.setPlayWhenReady(true);
            }
        }
    }

    private GVRVideoSceneObjectPlayer<MediaPlayer> makeMediaPlayer() {
        final MediaPlayer mediaPlayer = new MediaPlayer();
        final AssetFileDescriptor afd;
        final String url;

        try {
            url = "http://152.14.132.193/hls/local/index.m3u8";
            //afd = getAssets().openFd("videos_s_3.mp4");
            android.util.Log.d("Minimal360Video", "Assets was found.");
            //mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
            android.util.Log.d("Minimal360Video", "DataSource was set.");
            //afd.close();
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
            android.util.Log.e("Minimal360Video", "Assets were not loaded. Stopping application!");
            return null;
        }

        mediaPlayer.setLooping(true);
        android.util.Log.d("Minimal360Video", "starting player.");

        return GVRVideoSceneObject.makePlayerInstance(mediaPlayer);
    }

    //TODO upgrade code to use exoplayer2
    private GVRVideoSceneObjectPlayer<ExoPlayer> makeExoPlayer() {
        //We'll start the upgrade with a track selector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);




        //Creating a LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        //Renderer
        RenderersFactory renderer = new DefaultRenderersFactory(this);


        //Create the player
        Player.EventListener exoplayerEventListener;
        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(renderer,trackSelector,loadControl);

        String urimp4 = "/video.mp4"; //upload file to device and add path/name.mp4
        Uri mp4VideoUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+urimp4);

        Uri mp4HLSUri = Uri.parse("http://152.14.132.193/hls/local/index.m3u8");

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();

        //Produces DataSource instances through which media data is loaded.
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), bandwidthMeterA);

        //Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        //This is how you might play a local file
        MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);

        //MediaSource videoSource = new HlsMediaSource(mp4HLSUri,dataSourceFactory,1,null,null);
        final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);

        //might need to prepare the player

        //final AssetDataSource dataSource = new AssetDataSource(this);
        //final ExtractorSampleSource sampleSource = new ExtractorSampleSource(Uri.parse("asset:///videos_s_3.mp4"),
        //      dataSource, new DefaultAllocator(64 * 1024), 64 * 1024 * 256);

        player.prepare(videoSource);

        return new GVRVideoSceneObjectPlayer<ExoPlayer>() {
            @Override
            public ExoPlayer getPlayer() {
                return player;
            }

            @Override
            public void setSurface(final Surface surface) {
                player.addListener( new Player.EventListener(){
                    @Override
                    public void onTimelineChanged(Timeline timeline, Object manifest) {
                        Log.v(TAG,"Listener-onTimelineChanged...");

                    }

                    @Override
                    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                        Log.v(TAG,"Listener-onTracksChanged...");

                    }

                    @Override
                    public void onLoadingChanged(boolean isLoading) {
                        Log.v(TAG,"Listener-onLoadingChanged...");

                    }

                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        Log.e(TAG,"Listener-onPlayerStateChanged...");

                        switch (playbackState) {
                            case Player.STATE_BUFFERING:
                                break;
                            case Player.STATE_ENDED:
                                player.seekTo(0);
                                break;
                            case Player.STATE_IDLE:
                                break;
                            case Player.STATE_READY:
                                break;
                            default:
                                break;
                        }
                    }

                    @Override
                    public void onPlaybackParametersChanged(PlaybackParameters p){
                        Log.v(TAG, "Listener-onPlaybackParametersChanged...");
                    }

                    @Override
                    public void onRepeatModeChanged(int i){
                        Log.v(TAG, "Listener-onRepeatModeChanged...");
                    }

                    @Override
                    public void onPlayerError(ExoPlaybackException error) {
                        Log.v(TAG,"Listener-onPlayerError...");
                        player.stop();
                        player.prepare(loopingSource);
                        player.setPlayWhenReady(true);

                    }

                    @Override
                    public void onPositionDiscontinuity() {
                        Log.v(TAG,"Listener-onPositionDiscontinuity...");

                    }
                });

            }

            @Override
            public void release() {
                player.release();
            }

            @Override
            public boolean canReleaseSurfaceImmediately() {
                return false;
            }

            @Override
            public void pause() {
                player.setPlayWhenReady(false);
            }

            @Override
            public void start() {
                player.setPlayWhenReady(true);
            }
        };
    }

    private GVRVideoSceneObjectPlayer<?> videoSceneObjectPlayer;

    static final boolean USE_EXO_PLAYER = true;
}