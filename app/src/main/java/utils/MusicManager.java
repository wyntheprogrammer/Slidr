package com.example.slidr.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class MusicManager {
    private static final String TAG = "MusicManager";
    private static MediaPlayer mediaPlayer;
    private static int currentMusicResId = -1;
    private static boolean isPaused = false;

    /**
     * Start playing music
     * @param context Application context
     * @param musicResId Resource ID of the music file (R.raw.xxx)
     */
    public static void playMusic(Context context, int musicResId) {
        try {
            // If same music is already playing, don't restart
            if (mediaPlayer != null && currentMusicResId == musicResId && mediaPlayer.isPlaying()) {
                Log.d(TAG, "Music already playing");
                return;
            }

            // Stop current music if playing
            stopMusic();

            // Create and start new music
            if (musicResId != -1) {
                mediaPlayer = MediaPlayer.create(context.getApplicationContext(), musicResId);

                if (mediaPlayer != null) {
                    mediaPlayer.setLooping(true);
                    mediaPlayer.setVolume(0.7f, 0.7f);
                    mediaPlayer.start();
                    currentMusicResId = musicResId;
                    isPaused = false;
                    Log.d(TAG, "Music started: " + musicResId);
                } else {
                    Log.e(TAG, "Failed to create MediaPlayer for resource: " + musicResId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing music: " + e.getMessage());
        }
    }

    /**
     * Stop music completely
     */
    public static void stopMusic() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                currentMusicResId = -1;
                isPaused = false;
                Log.d(TAG, "Music stopped");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping music: " + e.getMessage());
        }
    }

    /**
     * Pause music (can be resumed later)
     */
    public static void pauseMusic() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPaused = true;
                Log.d(TAG, "Music paused");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error pausing music: " + e.getMessage());
        }
    }

    /**
     * Resume paused music
     */
    public static void resumeMusic() {
        try {
            if (mediaPlayer != null && isPaused) {
                mediaPlayer.start();
                isPaused = false;
                Log.d(TAG, "Music resumed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resuming music: " + e.getMessage());
        }
    }

    /**
     * Check if music is currently playing
     */
    public static boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set music volume
     * @param volume Volume level (0.0 to 1.0)
     */
    public static void setVolume(float volume) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volume, volume);
                Log.d(TAG, "Volume set to: " + volume);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting volume: " + e.getMessage());
        }
    }

    /**
     * Get current music resource ID
     */
    public static int getCurrentMusicResId() {
        return currentMusicResId;
    }
}