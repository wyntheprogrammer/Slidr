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

    // Game History
    @Insert
    void insertGame(GameHistory game);

    @Query("SELECT * FROM game_history ORDER BY timestamp DESC")
    List<GameHistory> getAllGames();

    @Query("SELECT * FROM game_history WHERE gridSize = :gridSize ORDER BY timestamp DESC LIMIT 10")
    List<GameHistory> getRecentGamesBySize(int gridSize);

    @Query("SELECT * FROM game_history WHERE completed = 1 AND gridSize = :gridSize ORDER BY moves ASC LIMIT 1")
    GameHistory getBestGameByMoves(int gridSize);

    @Query("SELECT * FROM game_history WHERE completed = 1 AND gridSize = :gridSize ORDER BY timeInSeconds ASC LIMIT 1")
    GameHistory getBestGameByTime(int gridSize);

    @Query("DELETE FROM game_history")
    void deleteAllGames();

    // Statistics
    @Insert
    void insertStatistics(Statistics statistics);

    @Update
    void updateStatistics(Statistics statistics);

    @Query("SELECT * FROM statistics WHERE gridSize = :gridSize")
    Statistics getStatistics(int gridSize);

    @Query("SELECT * FROM statistics")
    List<Statistics> getAllStatistics();

    @Query("DELETE FROM statistics")
    void deleteAllStatistics();

    // User Progress (Stars)
    @Insert
    void insertUserProgress(UserProgress progress);

    @Update
    void updateUserProgress(UserProgress progress);

    @Query("SELECT * FROM user_progress WHERE userId = 1")
    UserProgress getUserProgress();

    // Puzzle Unlocks
    @Insert
    void insertPuzzleUnlock(PuzzleUnlock unlock);

    @Update
    void updatePuzzleUnlock(PuzzleUnlock unlock);

    @Query("SELECT * FROM puzzle_unlocks WHERE storyMode = :storyMode AND arcIndex = :arcIndex")
    PuzzleUnlock getPuzzleUnlock(String storyMode, int arcIndex);

    @Query("SELECT * FROM puzzle_unlocks WHERE storyMode = :storyMode ORDER BY arcIndex ASC")
    List<PuzzleUnlock> getStoryModeProgress(String storyMode);

    @Query("SELECT * FROM puzzle_unlocks")
    List<PuzzleUnlock> getAllUnlocks();

    @Query("DELETE FROM puzzle_unlocks")
    void deleteAllUnlocks();

    // Music Tracks
    @Insert
    void insertMusicTrack(MusicTrack track);

    @Update
    void updateMusicTrack(MusicTrack track);

    @Query("SELECT * FROM music_tracks WHERE id = :id")
    MusicTrack getMusicTrack(int id);

    @Query("SELECT * FROM music_tracks WHERE unlocked = 1")
    List<MusicTrack> getUnlockedTracks();

    @Query("SELECT * FROM music_tracks ORDER BY id ASC")
    List<MusicTrack> getAllMusicTracks();

    @Query("SELECT * FROM music_tracks WHERE storyMode = :storyMode AND arcIndex = :arcIndex")
    MusicTrack getMusicForArc(String storyMode, int arcIndex);

    // Game Settings
    @Insert
    void insertSettings(GameSettings settings);

    @Update
    void updateSettings(GameSettings settings);

    @Query("SELECT * FROM game_settings WHERE id = 1")
    GameSettings getSettings();
}