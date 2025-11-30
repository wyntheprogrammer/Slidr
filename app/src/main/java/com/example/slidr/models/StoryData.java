package com.example.slidr.models;

import com.example.slidr.R;

public class StoryData {

    public static class StoryMode {
        public String id;
        public String name;
        public Story[] stories;
        public int color;

        public StoryMode(String id, String name, Story[] stories, int color) {
            this.id = id;
            this.name = name;
            this.stories = stories;
            this.color = color;
        }
    }

    public static class Story {
        public String name;
        public int imageResId;
        public int starsRequired; // Stars needed to unlock this story

        public Story(String name, int imageResId, int starsRequired) {
            this.name = name;
            this.imageResId = imageResId;
            this.starsRequired = starsRequired;
        }
    }

    // Story Mode Data
    public static StoryMode[] getStoryModes() {
        return new StoryMode[] {
                new StoryMode(
                        "onepiece",
                        "One Piece",
                        new Story[] {
                                new Story("The Straw Hat Promise", R.drawable.onepiece_story1, 0), // FREE
                                new Story("Luffy Sets Sail", R.drawable.onepiece_story2, 3),
                                new Story("The First Nakama", R.drawable.onepiece_story3, 6),
                                new Story("Luffy vs Buggy", R.drawable.onepiece_story4, 9)
                        },
                        0xFFFF6B35 // Orange
                ),
                new StoryMode(
                        "dragonball",
                        "Dragon Ball Z",
                        new Story[] {
                                new Story("Raditz Arrives", R.drawable.dragonball_story1, 0), // FREE
                                new Story("Goku vs Raditz", R.drawable.dragonball_story2, 3),
                                new Story("Piccolo's Training", R.drawable.dragonball_story3, 6),
                                new Story("Saiyan Warriors Arrive", R.drawable.dragonball_story4, 9)
                        },
                        0xFFFFA500 // Gold
                ),
                new StoryMode(
                        "bleach",
                        "Bleach",
                        new Story[] {
                                new Story("The Substitute Shinigami", R.drawable.bleach_story1, 0), // FREE
                                new Story("Hollow Attack", R.drawable.bleach_story2, 3),
                                new Story("The Strange Man", R.drawable.bleach_story3, 6),
                                new Story("Ichigo vs Renji", R.drawable.bleach_story4, 9)
                        },
                        0xFF4169E1 // Royal Blue
                )
        };
    }

    public static StoryMode getStoryMode(String id) {
        for (StoryMode mode : getStoryModes()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }
        return null;
    }
}