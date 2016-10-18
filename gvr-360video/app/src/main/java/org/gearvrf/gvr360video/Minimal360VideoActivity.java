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

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Surface;

import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TimeRange;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.chunk.ChunkSampleSource;
import com.google.android.exoplayer.chunk.ChunkSource;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.chunk.FormatEvaluator;
import com.google.android.exoplayer.dash.DashChunkSource;
import com.google.android.exoplayer.dash.DefaultDashTrackSelector;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescriptionParser;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.AssetDataSource;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;
import org.gearvrf.utility.Log;

import java.io.IOException;

public class Minimal360VideoActivity extends GVRActivity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!USE_EXO_PLAYER) {
            videoSceneObjectPlayer = makeMediaPlayer();
        } else {
            videoSceneObjectPlayer = makeExoPlayer();
        }

        if (null != videoSceneObjectPlayer) {
            final Minimal360Video main = new Minimal360Video(videoSceneObjectPlayer);
            setMain(main, "gvr.xml");
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

        try {
            afd = getAssets().openFd("videos_s_3.mp4");
            android.util.Log.d("Minimal360Video", "Assets was found.");
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            android.util.Log.d("Minimal360Video", "DataSource was set.");
            afd.close();
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

    Handler mainHandler;
    private GVRVideoSceneObjectPlayer<ExoPlayer> makeExoPlayer() {
        final ExoPlayer player = ExoPlayer.Factory.newInstance(2);

        MediaPresentationDescriptionParser parser = new MediaPresentationDescriptionParser();
        final String userAgent = "GVRF 3.x";
        final Context context = this;
        DefaultUriDataSource manifestDataSource = new DefaultUriDataSource(context, userAgent);
        final String url = "http://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAECCCB5C7.8506521BFC350652163895D4C26DEE124209AA9E&key=ik0";
        ManifestFetcher manifestFetcher = new ManifestFetcher<>(url, manifestDataSource, parser);

        mainHandler = new Handler();
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(mainHandler, new BandwidthMeter.EventListener() {
            @Override
            public void onBandwidthSample(int elapsedMs, long bytes, long bitrate) {
            }
        });

        DataSource videoDataSource = new DefaultUriDataSource(context, userAgent);
        ChunkSource videoChunkSource = new DashChunkSource(manifestFetcher,
                DefaultDashTrackSelector.newVideoInstance(context, true, false),
                videoDataSource, new FormatEvaluator.AdaptiveEvaluator(bandwidthMeter), 30000,
                0L, mainHandler, new DashChunkSource.EventListener() {
            @Override
            public void onAvailableRangeChanged(int sourceId, TimeRange availableRange) {

            }
        }, 0);
        ChunkSampleSource videoSampleSource = new ChunkSampleSource(videoChunkSource,
                new DefaultLoadControl(new DefaultAllocator(64 * 1024)),
                200 * 64 * 1024, mainHandler, new ChunkSampleSource.EventListener() {
            @Override
            public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs) {

            }

            @Override
            public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {

            }

            @Override
            public void onLoadCanceled(int sourceId, long bytesLoaded) {

            }

            @Override
            public void onLoadError(int sourceId, IOException e) {

            }

            @Override
            public void onUpstreamDiscarded(int sourceId, long mediaStartTimeMs, long mediaEndTimeMs) {

            }

            @Override
            public void onDownstreamFormatChanged(int sourceId, Format format, int trigger, long mediaTimeMs) {

            }
        }, 0);
        final TrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context, videoSampleSource,
                MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000,
                null, true, mainHandler, new MediaCodecVideoTrackRenderer.EventListener() {
            @Override
            public void onDroppedFrames(int count, long elapsed) {

            }

            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

            }

            @Override
            public void onDrawnToSurface(Surface surface) {

            }

            @Override
            public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {

            }

            @Override
            public void onCryptoError(MediaCodec.CryptoException e) {

            }

            @Override
            public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {

            }
        }, 50);

        // Build the audio renderer.
        DataSource audioDataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
        ChunkSource audioChunkSource = new DashChunkSource(manifestFetcher,
                DefaultDashTrackSelector.newAudioInstance(), audioDataSource, null, 30000,
                0L, mainHandler, new DashChunkSource.EventListener() {
            @Override
            public void onAvailableRangeChanged(int sourceId, TimeRange availableRange) {

            }
        }, 1);
        ChunkSampleSource audioSampleSource = new ChunkSampleSource(audioChunkSource,
                new DefaultLoadControl(new DefaultAllocator(64 * 1024)),
                54 * 64 * 1024, mainHandler, new ChunkSampleSource.EventListener() {
            @Override
            public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs) {

            }

            @Override
            public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {

            }

            @Override
            public void onLoadCanceled(int sourceId, long bytesLoaded) {

            }

            @Override
            public void onLoadError(int sourceId, IOException e) {

            }

            @Override
            public void onUpstreamDiscarded(int sourceId, long mediaStartTimeMs, long mediaEndTimeMs) {

            }

            @Override
            public void onDownstreamFormatChanged(int sourceId, Format format, int trigger, long mediaTimeMs) {

            }
        }, 1);
        TrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(audioSampleSource,
                MediaCodecSelector.DEFAULT, null, true, mainHandler, new MediaCodecAudioTrackRenderer.EventListener() {
            @Override
            public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {

            }

            @Override
            public void onAudioTrackWriteError(AudioTrack.WriteException e) {

            }

            @Override
            public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

            }

            @Override
            public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {

            }

            @Override
            public void onCryptoError(MediaCodec.CryptoException e) {

            }

            @Override
            public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {

            }
        },
                AudioCapabilities.getCapabilities(context), AudioManager.STREAM_MUSIC);

//        final AssetDataSource dataSource = new AssetDataSource(this);
//        final ExtractorSampleSource sampleSource = new ExtractorSampleSource(Uri.parse("asset:///videos_s_3.mp4"),
//                dataSource, new DefaultAllocator(64 * 1024), 64 * 1024 * 256);
//
//        final MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(this, sampleSource,
//                MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
//        final MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
//                MediaCodecSelector.DEFAULT);
        player.prepare(videoRenderer, audioRenderer);

        return new GVRVideoSceneObjectPlayer<ExoPlayer>() {
            @Override
            public ExoPlayer getPlayer() {
                return player;
            }

            @Override
            public void setSurface(final Surface surface) {
                player.addListener(new ExoPlayer.Listener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        switch (playbackState) {
                            case ExoPlayer.STATE_BUFFERING:
                                break;
                            case ExoPlayer.STATE_ENDED:
                                player.seekTo(0);
                                break;
                            case ExoPlayer.STATE_IDLE:
                                break;
                            case ExoPlayer.STATE_PREPARING:
                                break;
                            case ExoPlayer.STATE_READY:
                                break;
                            default:
                                break;
                        }
                    }

                    @Override
                    public void onPlayWhenReadyCommitted() {
                        surface.release();
                    }

                    @Override
                    public void onPlayerError(ExoPlaybackException error) {
                    }
                });

                player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
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
