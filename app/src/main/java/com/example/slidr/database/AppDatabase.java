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
}, version = 6, exportSchema = false)
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

            // Initialize music tracks (shared across all users)
            initializeMusicTracks(db);

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
        }).start();
    }

    private static void initializeMusicTracks(AppDatabase db) {
        List<MusicTrack> tracks = db.gameDao().getAllMusicTracks();
        if (!tracks.isEmpty()) {
            return;
        }

        // One Piece music tracks - ALL locked initially (except Arc 1)
        db.gameDao().insertMusicTrack(new MusicTrack(
                "We Are!", "onepiece", 0,
                com.example.slidr.R.raw.onepiece_arc1_music, false)); // Locked
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Believe", "onepiece", 1,
                com.example.slidr.R.raw.onepiece_arc2_music, false));
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Kokoro no Chizu", "onepiece", 2,
                com.example.slidr.R.raw.onepiece_arc3_music, false));
        db.gameDao().insertMusicTrack(new MusicTrack(
                "One Day", "onepiece", 3,
                com.example.slidr.R.raw.onepiece_arc4_music, false));

        // Dragon Ball Z music tracks - ALL locked initially
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Cha-La Head-Cha-La", "dragonball", 0,
                com.example.slidr.R.raw.dragonball_arc1_music, false));
        db.gameDao().insertMusicTrack(new MusicTrack(
                "We Gotta Power", "dragonball", 1,
                com.example.slidr.R.raw.dragonball_arc2_music, false));
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Dragon Soul", "dragonball", 2,
                com.example.slidr.R.raw.dragonball_arc3_music, false));
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Dan Dan Kokoro", "dragonball", 3,
                com.example.slidr.R.raw.dragonball_arc4_music, false));

        // Bleach music tracks - ALL locked initially
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Asterisk", "bleach", 0,
                com.example.slidr.R.raw.bleach_arc1_music, false));
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Ichirin no Hana", "bleach", 1,
                com.example.slidr.R.raw.bleach_arc2_music, false));
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Alones", "bleach", 2,
                com.example.slidr.R.raw.bleach_arc3_music, false));
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Ranbu no Melody", "bleach", 3,
                com.example.slidr.R.raw.bleach_arc4_music, false));
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