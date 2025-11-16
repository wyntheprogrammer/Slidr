package com.example.slidr.models;

import com.example.slidr.R;

public class StoryData {

    public static class StoryMode {
        public String id;
        public String name;
        public Arc[] arcs;
        public int color;

        public StoryMode(String id, String name, Arc[] arcs, int color) {
            this.id = id;
            this.name = name;
            this.arcs = arcs;
            this.color = color;
        }
    }

    public static class Arc {
        public String name;
        public int imageResId;
        public int starsRequired; // Stars needed to unlock this arc

        public Arc(String name, int imageResId, int starsRequired) {
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
                        new Arc[] {
                                new Arc("East Blue Saga", R.drawable.onepiece_arc1, 0), // FREE
                                new Arc("Alabasta Saga", R.drawable.onepiece_arc2, 3),
                                new Arc("Enies Lobby Saga", R.drawable.onepiece_arc3, 6),
                                new Arc("Marineford Saga", R.drawable.onepiece_arc4, 9)
                        },
                        0xFFFF6B35 // Orange
                ),
                new StoryMode(
                        "dragonball",
                        "Dragon Ball Z",
                        new Arc[] {
                                new Arc("Saiyan Saga", R.drawable.dragonball_arc1, 0), // FREE
                                new Arc("Frieza Saga", R.drawable.dragonball_arc2, 3),
                                new Arc("Cell Saga", R.drawable.dragonball_arc3, 6),
                                new Arc("Buu Saga", R.drawable.dragonball_arc4, 9)
                        },
                        0xFFFFA500 // Gold
                ),
                new StoryMode(
                        "bleach",
                        "Bleach",
                        new Arc[] {
                                new Arc("Soul Society Arc", R.drawable.bleach_arc1, 0), // FREE
                                new Arc("Arrancar Arc", R.drawable.bleach_arc2, 3),
                                new Arc("Fullbring Arc", R.drawable.bleach_arc3, 6),
                                new Arc("Quincy War Arc", R.drawable.bleach_arc4, 9)
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