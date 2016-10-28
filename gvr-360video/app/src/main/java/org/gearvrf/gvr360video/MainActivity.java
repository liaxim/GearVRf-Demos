package org.gearvrf.gvr360video;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.exoplayer.DefaultLoadControl;
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
import com.google.android.exoplayer.dash.mpd.AdaptationSet;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescription;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescriptionParser;
import com.google.android.exoplayer.dash.mpd.Period;
import com.google.android.exoplayer.dash.mpd.Representation;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.ManifestFetcher.ManifestCallback;

import org.gearvrf.utility.Log;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity implements SurfaceHolder.Callback {

    Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
        mainHandler = new Handler();

        MediaPresentationDescriptionParser parser = new MediaPresentationDescriptionParser();


        //DefaultUriDataSource manifestDataSource = new DefaultUriDataSource(context, userAgent);
        final DefaultHttpDataSource manifestDataSource = new DefaultHttpDataSource(userAgent, null);

        final String url = "http://yt-dash-mse-test.commondatastorage.googleapis.com/media/feelings_vp9-20130806-manifest.mpd";
        final ManifestFetcher<MediaPresentationDescription> manifestFetcher = new ManifestFetcher<MediaPresentationDescription>(url, manifestDataSource, parser);
        manifestFetcher.singleLoad(mainHandler.getLooper(), manifestFetcherCallback);
    }

    final String userAgent = "GVRF 3.x";

    private final ManifestCallback manifestFetcherCallback = new ManifestCallback<MediaPresentationDescription>() {
        @Override
        public void onSingleManifest(MediaPresentationDescription manifest) {
            player = ExoPlayer.Factory.newInstance(2);

            LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(64*1024));
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(null, null);

            Representation audioRepresentation = null;
            boolean audioRepresentationIsOpus = false;
            ArrayList<Representation> videoRepresentationsList = new ArrayList<>();
            Period period = manifest.getPeriod(0);
            for (int i = 0; i < period.adaptationSets.size(); i++) {
                AdaptationSet adaptationSet = period.adaptationSets.get(i);
                int adaptationSetType = adaptationSet.type;
                for (int j = 0; j < adaptationSet.representations.size(); j++) {
                    Representation representation = adaptationSet.representations.get(j);
                    String codecs = representation.format.codecs;
                    if (adaptationSetType == AdaptationSet.TYPE_AUDIO && audioRepresentation == null) {
                        audioRepresentation = representation;
                        audioRepresentationIsOpus = !TextUtils.isEmpty(codecs) && codecs.startsWith("opus");
                    } else if (adaptationSetType == AdaptationSet.TYPE_VIDEO && !TextUtils.isEmpty(codecs)
                            && codecs.startsWith("vp9")) {
                        videoRepresentationsList.add(representation);
                    }
                }
            }
            Representation[] videoRepresentations = new Representation[videoRepresentationsList.size()];
            DataSource videoDataSource = new DefaultUriDataSource(MainActivity.this, userAgent);
            ChunkSource videoChunkSource = new DashChunkSource(
                    DefaultDashTrackSelector.newVideoInstance(null, true, false),
                    videoDataSource,
                    new FormatEvaluator.AdaptiveEvaluator(bandwidthMeter),
                    manifest.getPeriodDuration(0),
                    AdaptationSet.TYPE_VIDEO, videoRepresentationsList);

            ChunkSampleSource videoSampleSource = new ChunkSampleSource(videoChunkSource, loadControl,
                    200 * 64*1024);

            videoRenderer = new LibvpxVideoTrackRenderer(videoSampleSource,
                    true, player.getMainHandler(), player, 50);
        }

        @Override
        public void onSingleManifestError(IOException e) {
            Log.i("mmarinov", "onSingleManifestError: " + e);
            finish();
        }
    };
    ExoPlayer player;

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
//        manifestFetcher.singleLoad(mainHandler.getLooper(), new ManifestCallback() {
//            @Override
//            public void onSingleManifest(Object manifest) {
//                Log.i("mmarinov", "onSingleManifest");
//
//                DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(mainHandler, new BandwidthMeter.EventListener() {
//                    @Override
//                    public void onBandwidthSample(int elapsedMs, long bytes, long bitrate) {
//                        Log.i("mmarinov", "onBandwidthSample");
//                    }
//                });
//
//                DataSource videoDataSource = new DefaultUriDataSource(context, userAgent);
//                ChunkSource videoChunkSource = new DashChunkSource(manifestFetcher,
//                        DefaultDashTrackSelector.newVideoInstance(context, true, false),
//                        videoDataSource, new FormatEvaluator.AdaptiveEvaluator(bandwidthMeter), 30000,
//                        0L, mainHandler, new DashChunkSource.EventListener() {
//                    @Override
//                    public void onAvailableRangeChanged(int sourceId, TimeRange availableRange) {
//                        Log.i("mmarinov", "onAvailableRangeChanged");
//                    }
//                }, 0);
//                ChunkSampleSource videoSampleSource = new ChunkSampleSource(videoChunkSource,
//                        new DefaultLoadControl(new DefaultAllocator(64 * 1024)),
//                        200 * 64 * 1024, mainHandler, new ChunkSampleSource.EventListener() {
//                    @Override
//                    public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs) {
//                        Log.i("mmarinov", "onLoadStarted");
//                    }
//
//                    @Override
//                    public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {
//                        Log.i("mmarinov", "onLoadCompleted");
//                    }
//
//                    @Override
//                    public void onLoadCanceled(int sourceId, long bytesLoaded) {
//                        Log.i("mmarinov", "onLoadCanceled");
//                    }
//
//                    @Override
//                    public void onLoadError(int sourceId, IOException e) {
//                        Log.i("mmarinov", "onLoadError");
//                    }
//
//                    @Override
//                    public void onUpstreamDiscarded(int sourceId, long mediaStartTimeMs, long mediaEndTimeMs) {
//                        Log.i("mmarinov", "onUpstreamDiscarded");
//                    }
//
//                    @Override
//                    public void onDownstreamFormatChanged(int sourceId, Format format, int trigger, long mediaTimeMs) {
//                        Log.i("mmarinov", "onDownstreamFormatChanged");
//                    }
//                }, 0);
//                final TrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context, videoSampleSource,
//                        MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000,
//                        null, true, mainHandler, new MediaCodecVideoTrackRenderer.EventListener() {
//                    @Override
//                    public void onDroppedFrames(int count, long elapsed) {
//                        Log.i("mmarinov", "onDroppedFrames");
//                    }
//
//                    @Override
//                    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
//                        Log.i("mmarinov", "onVideoSizeChanged");
//                    }
//
//                    @Override
//                    public void onDrawnToSurface(Surface surface) {
//                        Log.i("mmarinov", "onDrawnToSurface");
//                    }
//
//                    @Override
//                    public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
//                        Log.i("mmarinov", "onDecoderInitializationError");
//                    }
//
//                    @Override
//                    public void onCryptoError(MediaCodec.CryptoException e) {
//                        Log.i("mmarinov", "onCryptoError");
//                    }
//
//                    @Override
//                    public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {
//                        Log.i("mmarinov", "onDecoderInitialized");
//                    }
//                }, 50);
//
//                // Build the audio renderer.
//                DataSource audioDataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
//                ChunkSource audioChunkSource = new DashChunkSource(manifestFetcher,
//                        DefaultDashTrackSelector.newAudioInstance(), audioDataSource, null, 30000,
//                        0L, mainHandler, new DashChunkSource.EventListener() {
//                    @Override
//                    public void onAvailableRangeChanged(int sourceId, TimeRange availableRange) {
//                        Log.i("mmarinov", "onAvailableRangeChanged");
//                    }
//                }, 1);
//                ChunkSampleSource audioSampleSource = new ChunkSampleSource(audioChunkSource,
//                        new DefaultLoadControl(new DefaultAllocator(64 * 1024)),
//                        54 * 64 * 1024, mainHandler, new ChunkSampleSource.EventListener() {
//                    @Override
//                    public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs) {
//                        Log.i("mmarinov", "onLoadStarted");
//                    }
//
//                    @Override
//                    public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {
//                        Log.i("mmarinov", "onLoadCompleted");
//                    }
//
//                    @Override
//                    public void onLoadCanceled(int sourceId, long bytesLoaded) {
//                        Log.i("mmarinov", "onLoadCanceled");
//                    }
//
//                    @Override
//                    public void onLoadError(int sourceId, IOException e) {
//                        Log.i("mmarinov", "onLoadError");
//                    }
//
//                    @Override
//                    public void onUpstreamDiscarded(int sourceId, long mediaStartTimeMs, long mediaEndTimeMs) {
//                        Log.i("mmarinov", "onUpstreamDiscarded");
//                    }
//
//                    @Override
//                    public void onDownstreamFormatChanged(int sourceId, Format format, int trigger, long mediaTimeMs) {
//                        Log.i("mmarinov", "onDownstreamFormatChanged");
//                    }
//                }, 1);
//                TrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(audioSampleSource,
//                        MediaCodecSelector.DEFAULT, null, true, mainHandler, new MediaCodecAudioTrackRenderer.EventListener() {
//                    @Override
//                    public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
//                        Log.i("mmarinov", "onAudioTrackInitializationError");
//                    }
//
//                    @Override
//                    public void onAudioTrackWriteError(AudioTrack.WriteException e) {
//                        Log.i("mmarinov", "onAudioTrackWriteError");
//                    }
//
//                    @Override
//                    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
//                        Log.i("mmarinov", "onAudioTrackUnderrun");
//                    }
//
//                    @Override
//                    public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
//                        Log.i("mmarinov", "onDecoderInitializationError");
//                    }
//
//                    @Override
//                    public void onCryptoError(MediaCodec.CryptoException e) {
//                        Log.i("mmarinov", "onCryptoError");
//                    }
//
//                    @Override
//                    public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {
//                        Log.i("mmarinov", "onDecoderInitialized");
//                    }
//                },
//                        AudioCapabilities.getCapabilities(context), AudioManager.STREAM_MUSIC);
//
//                player.prepare(videoRenderer, audioRenderer);
//                player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, holder.getSurface());
//            }
//
//            @Override
//            public void onSingleManifestError(IOException e) {
//                Log.i("mmarinov", "onSingleManifestError " + e);
//            }
//        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}