package com.example.slidr.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GameDao {

    // User Authentication
    @Insert
    void insertUser(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    User getLoggedInUser();

    @Query("UPDATE users SET isLoggedIn = 0")
    void logoutAllUsers();

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    // Game History - Now per user
    @Insert
    void insertGame(GameHistory game);

    @Query("SELECT * FROM game_history WHERE userId = :userId ORDER BY timestamp DESC")
    List<GameHistory> getAllGamesByUser(int userId);

    // Get current logged-in user's game history
    @Query("SELECT gh.* FROM game_history gh INNER JOIN users u ON gh.userId = u.id WHERE u.isLoggedIn = 1 ORDER BY gh.timestamp DESC")
    List<GameHistory> getAllGames();

    @Query("SELECT gh.* FROM game_history gh INNER JOIN users u ON gh.userId = u.id WHERE u.isLoggedIn = 1 AND gh.gridSize = :gridSize ORDER BY gh.timestamp DESC LIMIT 10")
    List<GameHistory> getRecentGamesBySize(int gridSize);

    @Query("SELECT gh.* FROM game_history gh INNER JOIN users u ON gh.userId = u.id WHERE u.isLoggedIn = 1 AND gh.completed = 1 AND gh.gridSize = :gridSize ORDER BY gh.moves ASC LIMIT 1")
    GameHistory getBestGameByMoves(int gridSize);

    @Query("SELECT gh.* FROM game_history gh INNER JOIN users u ON gh.userId = u.id WHERE u.isLoggedIn = 1 AND gh.completed = 1 AND gh.gridSize = :gridSize ORDER BY gh.timeInSeconds ASC LIMIT 1")
    GameHistory getBestGameByTime(int gridSize);

    @Query("DELETE FROM game_history WHERE userId IN (SELECT id FROM users WHERE isLoggedIn = 1)")
    void deleteAllGames();

    // Statistics - Now per user
    @Insert
    void insertStatistics(Statistics statistics);

    @Update
    void updateStatistics(Statistics statistics);

    @Query("SELECT * FROM statistics WHERE userId = :userId AND gridSize = :gridSize LIMIT 1")
    Statistics getStatisticsByUser(int userId, int gridSize);

    // Get current logged-in user's statistics
    @Query("SELECT s.* FROM statistics s INNER JOIN users u ON s.userId = u.id WHERE u.isLoggedIn = 1 AND s.gridSize = :gridSize LIMIT 1")
    Statistics getStatistics(int gridSize);

    @Query("SELECT s.* FROM statistics s INNER JOIN users u ON s.userId = u.id WHERE u.isLoggedIn = 1")
    List<Statistics> getAllStatistics();

    @Query("DELETE FROM statistics WHERE userId IN (SELECT id FROM users WHERE isLoggedIn = 1)")
    void deleteAllStatistics();

    // User Progress (Stars) - Now per user
    @Insert
    void insertUserProgress(UserProgress progress);

    @Update
    void updateUserProgress(UserProgress progress);

    @Query("SELECT * FROM user_progress WHERE userId = :userId LIMIT 1")
    UserProgress getUserProgressByUserId(int userId);

    // Get current logged-in user's progress
    @Query("SELECT up.* FROM user_progress up INNER JOIN users u ON up.userId = u.id WHERE u.isLoggedIn = 1 LIMIT 1")
    UserProgress getUserProgress();

    // Puzzle Unlocks - Now per user
    @Insert
    void insertPuzzleUnlock(PuzzleUnlock unlock);

    @Update
    void updatePuzzleUnlock(PuzzleUnlock unlock);

    @Query("SELECT * FROM puzzle_unlocks WHERE userId = :userId AND storyMode = :storyMode AND storyIndex = :storyIndex LIMIT 1")
    PuzzleUnlock getPuzzleUnlockByUser(int userId, String storyMode, int storyIndex);

    @Query("SELECT * FROM puzzle_unlocks WHERE userId = :userId AND storyMode = :storyMode ORDER BY storyIndex ASC")
    List<PuzzleUnlock> getStoryModeProgressByUser(int userId, String storyMode);

    // Get current logged-in user's puzzle unlocks
    @Query("SELECT pu.* FROM puzzle_unlocks pu INNER JOIN users u ON pu.userId = u.id WHERE u.isLoggedIn = 1 AND pu.storyMode = :storyMode AND pu.storyIndex = :storyIndex LIMIT 1")
    PuzzleUnlock getPuzzleUnlock(String storyMode, int storyIndex);

    @Query("SELECT pu.* FROM puzzle_unlocks pu INNER JOIN users u ON pu.userId = u.id WHERE u.isLoggedIn = 1 AND pu.storyMode = :storyMode ORDER BY pu.storyIndex ASC")
    List<PuzzleUnlock> getStoryModeProgress(String storyMode);

    @Query("SELECT * FROM puzzle_unlocks")
    List<PuzzleUnlock> getAllUnlocks();

    @Query("DELETE FROM puzzle_unlocks")
    void deleteAllUnlocks();

    // Music Tracks - NOW USER-SPECIFIC
    @Insert
    void insertMusicTrack(MusicTrack track);

    @Update
    void updateMusicTrack(MusicTrack track);

    @Query("SELECT * FROM music_tracks WHERE id = :id")
    MusicTrack getMusicTrack(int id);

    // Get music tracks for specific user
    @Query("SELECT * FROM music_tracks WHERE userId = :userId AND unlocked = 1")
    List<MusicTrack> getUnlockedTracksByUser(int userId);

    // Get current logged-in user's unlocked tracks
    @Query("SELECT mt.* FROM music_tracks mt INNER JOIN users u ON mt.userId = u.id WHERE u.isLoggedIn = 1 AND mt.unlocked = 1")
    List<MusicTrack> getUnlockedTracks();

    // Get all music tracks for specific user
    @Query("SELECT * FROM music_tracks WHERE userId = :userId ORDER BY id ASC")
    List<MusicTrack> getAllMusicTracksByUser(int userId);

    // Get current logged-in user's music tracks
    @Query("SELECT mt.* FROM music_tracks mt INNER JOIN users u ON mt.userId = u.id WHERE u.isLoggedIn = 1 ORDER BY mt.id ASC")
    List<MusicTrack> getAllMusicTracks();

    // Get music for specific story for specific user
    @Query("SELECT * FROM music_tracks WHERE userId = :userId AND storyMode = :storyMode AND storyIndex = :storyIndex LIMIT 1")
    MusicTrack getMusicForStoryByUser(int userId, String storyMode, int storyIndex);

    // Get current logged-in user's music for story
    @Query("SELECT mt.* FROM music_tracks mt INNER JOIN users u ON mt.userId = u.id WHERE u.isLoggedIn = 1 AND mt.storyMode = :storyMode AND mt.storyIndex = :storyIndex LIMIT 1")
    MusicTrack getMusicForStory(String storyMode, int storyIndex);

    // Game Settings - NOW USER-SPECIFIC
    @Insert
    void insertSettings(GameSettings settings);

    @Update
    void updateSettings(GameSettings settings);

    // Get settings for specific user
    @Query("SELECT * FROM game_settings WHERE userId = :userId LIMIT 1")
    GameSettings getSettingsByUser(int userId);

    // Get current logged-in user's settings
    @Query("SELECT gs.* FROM game_settings gs INNER JOIN users u ON gs.userId = u.id WHERE u.isLoggedIn = 1 LIMIT 1")
    GameSettings getSettings();
}