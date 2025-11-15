package com.example.slidr.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.slidr.models.StoryData;

import java.util.List;

@Database(entities = {
        GameHistory.class,
        Statistics.class,
        UserProgress.class,
        PuzzleUnlock.class,
        MusicTrack.class,
        GameSettings.class
}, version = 3, exportSchema = false)
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
                    .fallbackToDestructiveMigration() // For version changes
                    .allowMainThreadQueries() // For simplicity; use executors in production
                    .build();

            // Initialize default data
            initializeDefaultData(instance);
        }
        return instance;
    }

    private static void initializeDefaultData(AppDatabase db) {
        new Thread(() -> {
            // Initialize user progress if not exists
            UserProgress progress = db.gameDao().getUserProgress();
            if (progress == null) {
                db.gameDao().insertUserProgress(new UserProgress());
            }

            // Initialize settings if not exists
            GameSettings settings = db.gameDao().getSettings();
            if (settings == null) {
                db.gameDao().insertSettings(new GameSettings());
            }

            // Initialize music tracks
            initializeMusicTracks(db);

            // Initialize story mode unlocks - first arc of each story is unlocked
            String[] storyModes = {"onepiece", "dragonball", "bleach"};
            for (String story : storyModes) {
                // Check if already initialized
                PuzzleUnlock firstArc = db.gameDao().getPuzzleUnlock(story, 0);
                if (firstArc == null) {
                    // Unlock first arc, lock the rest
                    int arcCount = getArcCount(story);
                    for (int i = 0; i < arcCount; i++) {
                        boolean unlocked = (i == 0); // Only first arc unlocked
                        db.gameDao().insertPuzzleUnlock(
                                new PuzzleUnlock(story, i, unlocked, 0)
                        );
                    }
                }
            }
        }).start();
    }

    private static void initializeMusicTracks(AppDatabase db) {
        // Check if music tracks already initialized
        List<MusicTrack> tracks = db.gameDao().getAllMusicTracks();
        if (!tracks.isEmpty()) {
            return; // Already initialized
        }

        // One Piece music tracks (replace R.raw.xxx with your actual music files)
        db.gameDao().insertMusicTrack(new MusicTrack(
                "We Are!", "onepiece", 0,
                com.example.slidr.R.raw.onepiece_arc1_music, true)); // Arc 1 unlocked by default
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Believe", "onepiece", 1,
                com.example.slidr.R.raw.onepiece_arc2_music, false));
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Kokoro no Chizu", "onepiece", 2,
                com.example.slidr.R.raw.onepiece_arc3_music, false));
        db.gameDao().insertMusicTrack(new MusicTrack(
                "One Day", "onepiece", 3,
                com.example.slidr.R.raw.onepiece_arc4_music, false));

        // Dragon Ball Z music tracks
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Cha-La Head-Cha-La", "dragonball", 0,
                com.example.slidr.R.raw.dragonball_arc1_music, true));
        db.gameDao().insertMusicTrack(new MusicTrack(
                "We Gotta Power", "dragonball", 1,
                com.example.slidr.R.raw.dragonball_arc2_music, false));
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Dragon Soul", "dragonball", 2,
                com.example.slidr.R.raw.dragonball_arc3_music, false));
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Dan Dan Kokoro", "dragonball", 3,
                com.example.slidr.R.raw.dragonball_arc4_music, false));

        // Bleach music tracks
        db.gameDao().insertMusicTrack(new MusicTrack(
                "Asterisk", "bleach", 0,
                com.example.slidr.R.raw.bleach_arc1_music, true));
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
            case "onepiece": return 4; // East Blue, Alabasta, Enies Lobby, Marineford
            case "dragonball": return 4; // Saiyan, Frieza, Cell, Buu
            case "bleach": return 4; // Soul Society, Arrancar, Fullbring, Quincy War
            default: return 4;
        }
    }
}