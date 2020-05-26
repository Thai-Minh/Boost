package com.gmc.libs.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Toast;

public class GmcVideoView extends TextureView implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnInfoListener,
        TextureView.SurfaceTextureListener {

    private MediaPlayer mediaPlayer;
    private MediaPlayer audioPlayer;
    private Uri videoPath;
    private Uri audioPath;
    private Surface surface;
    private VideoViewCallback videoViewCallback;
    private boolean prepared;
    private boolean needReset;
    private float bgVolume;
    private float movieVolume;
    private float maxVolume;
    private int positionPaused = -1;

    public GmcVideoView(Context context) {
        super(context);
    }

    public GmcVideoView(Context context, Uri videoPath) {
        super(context);

        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        this.bgVolume = maxVolume;
        this.movieVolume = maxVolume;

        this.videoPath = videoPath;
        setSurfaceTextureListener(this);
        setOpaque(false);
        createMediaPlayer();

    }

    public void setVideoPath(Uri videoPath) {
        needReset = true;
        this.videoPath = videoPath;
    }

    public Uri getAudioPath() {
        return audioPath;
    }

    public float getBgVolume() {
        return bgVolume;
    }

    public float getMovieVolume() {
        return movieVolume;
    }

    public boolean isNeedReset() {
        return needReset;
    }

    private void createMediaPlayer() {
        mediaPlayer = new MediaPlayer();
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.setSurface(null);
            mediaPlayer.release();
            mediaPlayer = null;
            positionPaused = -1;
        }

        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;
        }
    }

    public void removeAudio() {
        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;
            positionPaused = -1;
        }

        this.audioPath = null;
    }

    public void setAudioPath(Uri audioPath) {
        this.audioPath = audioPath;

        if (audioPlayer == null) {
            audioPlayer = new MediaPlayer();
        }

        if (audioPath != null && URLUtil.isValidUrl(audioPath.toString())) {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            }

            try {
                audioPlayer.reset();
                audioPlayer.setDataSource(getContext(), audioPath);
                audioPlayer.setVolume(bgVolume / maxVolume, bgVolume / maxVolume);
                audioPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setAudioPath(Uri audioPath, boolean autoPlay) {
        setAudioPath(audioPath);

        if (autoPlay) {
            if (audioPlayer.getDuration() > positionPaused) {
                audioPlayer.seekTo(positionPaused);
            }

            audioPlayer.start();
        }
    }

    public void setVolume(float bgVolume, float movieVolume) {
        this.bgVolume = bgVolume;
        this.movieVolume = movieVolume;

        if (mediaPlayer != null) {
            mediaPlayer.setVolume(movieVolume / maxVolume, movieVolume / maxVolume);
        }

        if (audioPlayer != null) {
            audioPlayer.setVolume(bgVolume / maxVolume, bgVolume / maxVolume);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void startPlayback() {
        if (mediaPlayer != null) {
            if (needReset) {
                mediaPlayer.seekTo(200);
            }
            mediaPlayer.start();
        }

        if (audioPlayer != null) {
            audioPlayer.start();
        } else if (audioPath != null && URLUtil.isValidUrl(audioPath.toString())) {
            setAudioPath(audioPath, true);
        }

    }

    public void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            positionPaused = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        }

        if (audioPlayer != null && audioPlayer.isPlaying())
            audioPlayer.pause();
    }

    public void resumePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.start();

        if (audioPlayer != null && audioPlayer.isPlaying())
            audioPlayer.start();
    }

    public boolean isPrepared() {
        return prepared;
    }

    public void setVideoViewCallback(VideoViewCallback videoViewCallback) {
        this.videoViewCallback = videoViewCallback;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        positionPaused = -1;

        if (videoViewCallback != null) {
            videoViewCallback.onVideoComplete();
        }

        if (audioPlayer != null) {
            audioPlayer.seekTo(0);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        prepared = true;
        if (videoViewCallback != null) {
            videoViewCallback.onVideoPrepared();
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surface = new Surface(surface);
        loadMedia();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (this.surface != null) {
            release();
            this.surface.release();
            this.surface = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void loadMedia() {
        needReset = false;

        if (surface == null)
            return;

        if (mediaPlayer == null) {
            createMediaPlayer();
        }

        try {
            mediaPlayer.reset();
            mediaPlayer.setSurface(surface);
            mediaPlayer.setDataSource(getContext(), videoPath);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(movieVolume / maxVolume, movieVolume / maxVolume);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface VideoViewCallback {
        void onVideoPrepared();
        void onVideoComplete();
    }
}
