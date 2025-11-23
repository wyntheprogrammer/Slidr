package com.example.slidr.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.List;

@Database(entities = {
        User.class,
        GameHistory.class,
        Statistics.class,
        UserProgress.class,
        PuzzleUnlock.class,
        MusicTrack.class,
        GameSettings.class
}, version = 7, exportSchema = false)  // Incremented version to 7
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract GameDao gameDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "slidr_database"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();

            initializeDefaultData(instance);
        }
        return instance;
    }

    private static void initializeDefaultData(AppDatabase db) {
        new Thread(() -> {
            // Initialize settings if not exists
            GameSettings settings = db.gameDao().getSettings();
            if (settings == null) {
                db.gameDao().insertSettings(new GameSettings());
            }

            // Initialize user-specific data for logged-in user
            User currentUser = db.gameDao().getLoggedInUser();
            if (currentUser != null) {
                initializeUserData(db, currentUser.getId());
            }
        }).start();
    }

    public static void initializeUserData(AppDatabase db, int userId) {
        new Thread(() -> {
            // Initialize user progress if not exists
            UserProgress progress = db.gameDao().getUserProgressByUserId(userId);
            if (progress == null) {
                progress = new UserProgress(userId);
                db.gameDao().insertUserProgress(progress);
            }

            // Initialize story mode unlocks for this user
            String[] storyModes = {"onepiece", "dragonball", "bleach"};
            for (String story : storyModes) {
                PuzzleUnlock firstArc = db.gameDao().getPuzzleUnlockByUser(userId, story, 0);
                if (firstArc == null) {
                    int arcCount = getArcCount(story);
                    for (int i = 0; i < arcCount; i++) {
                        boolean unlocked = (i == 0); // First arc is free
                        db.gameDao().insertPuzzleUnlock(
                                new PuzzleUnlock(userId, story, i, unlocked, 0)
                        );
                    }
                }
            }

            // Initialize music tracks for this user (USER-SPECIFIC)
            initializeMusicTracksForUser(db, userId);
        }).start();
    }

    private static void initializeMusicTracksForUser(AppDatabase db, int userId) {
        // Check if user already has music tracks initialized
        List<MusicTrack> userTracks = db.gameDao().getAllMusicTracksByUser(userId);
        if (!userTracks.isEmpty()) {
            return; // User already has music tracks
        }

        // Create music tracks for this user - ALL LOCKED initially
        // One Piece music tracks
        MusicTrack op1 = new MusicTrack(
                "We Are!", "onepiece", 0,
                com.example.slidr.R.raw.onepiece_arc1_music, false);
        op1.setUserId(userId);
        db.gameDao().insertMusicTrack(op1);

        MusicTrack op2 = new MusicTrack(
                "Believe", "onepiece", 1,
                com.example.slidr.R.raw.onepiece_arc2_music, false);
        op2.setUserId(userId);
        db.gameDao().insertMusicTrack(op2);

        MusicTrack op3 = new MusicTrack(
                "Kokoro no Chizu", "onepiece", 2,
                com.example.slidr.R.raw.onepiece_arc3_music, false);
        op3.setUserId(userId);
        db.gameDao().insertMusicTrack(op3);

        MusicTrack op4 = new MusicTrack(
                "One Day", "onepiece", 3,
                com.example.slidr.R.raw.onepiece_arc4_music, false);
        op4.setUserId(userId);
        db.gameDao().insertMusicTrack(op4);

        // Dragon Ball Z music tracks
        MusicTrack dbz1 = new MusicTrack(
                "Cha-La Head-Cha-La", "dragonball", 0,
                com.example.slidr.R.raw.dragonball_arc1_music, false);
        dbz1.setUserId(userId);
        db.gameDao().insertMusicTrack(dbz1);

        MusicTrack dbz2 = new MusicTrack(
                "We Gotta Power", "dragonball", 1,
                com.example.slidr.R.raw.dragonball_arc2_music, false);
        dbz2.setUserId(userId);
        db.gameDao().insertMusicTrack(dbz2);

        MusicTrack dbz3 = new MusicTrack(
                "Dragon Soul", "dragonball", 2,
                com.example.slidr.R.raw.dragonball_arc3_music, false);
        dbz3.setUserId(userId);
        db.gameDao().insertMusicTrack(dbz3);

        MusicTrack dbz4 = new MusicTrack(
                "Dan Dan Kokoro", "dragonball", 3,
                com.example.slidr.R.raw.dragonball_arc4_music, false);
        dbz4.setUserId(userId);
        db.gameDao().insertMusicTrack(dbz4);

        // Bleach music tracks
        MusicTrack bleach1 = new MusicTrack(
                "Asterisk", "bleach", 0,
                com.example.slidr.R.raw.bleach_arc1_music, false);
        bleach1.setUserId(userId);
        db.gameDao().insertMusicTrack(bleach1);

        MusicTrack bleach2 = new MusicTrack(
                "Ichirin no Hana", "bleach", 1,
                com.example.slidr.R.raw.bleach_arc2_music, false);
        bleach2.setUserId(userId);
        db.gameDao().insertMusicTrack(bleach2);

        MusicTrack bleach3 = new MusicTrack(
                "Alones", "bleach", 2,
                com.example.slidr.R.raw.bleach_arc3_music, false);
        bleach3.setUserId(userId);
        db.gameDao().insertMusicTrack(bleach3);

        MusicTrack bleach4 = new MusicTrack(
                "Ranbu no Melody", "bleach", 3,
                com.example.slidr.R.raw.bleach_arc4_music, false);
        bleach4.setUserId(userId);
        db.gameDao().insertMusicTrack(bleach4);
    }

    private static int getArcCount(String storyMode) {
        switch (storyMode) {
            case "onepiece": return 4;
            case "dragonball": return 4;
            case "bleach": return 4;
            default: return 4;
        }
    }
}