package com.fitnessproject.core.planner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlanPersonalizationEngine {
    private static final String[] PUSH_KEYWORDS = {
            "bench", "chest", "pec", "pecs", "push", "press", "upper body", "upper chest",
            "incline", "chest press", "pec deck"
    };
    private static final String[] PULL_KEYWORDS = {
            "back", "lat", "lats", "row", "rows", "pull", "pullup", "pull-up", "pull up",
            "chinup", "chin-up", "chin up", "rear delt", "upper back", "mid back"
    };
    private static final String[] LEGS_KEYWORDS = {
            "leg", "legs", "quad", "quads", "hamstring", "hamstrings", "glute", "glutes",
            "lower body", "squat", "lunge", "lunges", "deadlift", "calf", "calves"
    };
    private static final String[] ARMS_KEYWORDS = {
            "arm", "arms", "bicep", "biceps", "tricep", "triceps", "forearm", "forearms",
            "curl", "curls", "pushdown"
    };
    private static final String[] CORE_KEYWORDS = {
            "core", "abs", "ab", "abdominal", "abdominals", "stomach", "midsection",
            "waist", "oblique", "obliques", "trunk", "brace", "bracing"
    };
    private static final String[] CONDITIONING_KEYWORDS = {
            "5k", "10k", "marathon", "run", "running", "jog", "jogging", "cardio",
            "conditioning", "stamina", "endurance", "engine", "aerobic", "work capacity",
            "athletic", "bike", "cycling", "row", "rowing", "interval", "intervals"
    };

    /*
     * We keep personalization additive and deterministic.
     * The base template stays reliable, and specific notes only layer in a few
     * compatible accessories instead of trying to invent an entire plan from text.
     */
    public PlanTemplate applySpecificGoalNotes(PlanTemplate template, String specificNotes) {
        if (specificNotes == null || specificNotes.trim().isEmpty()) {
            return template;
        }

        String normalizedNotes = specificNotes.toLowerCase(Locale.US);
        List<PlanDay> personalizedDays = new ArrayList<>();
        
        for (PlanDay day : template.getDays()) {
            List<PlanExercise> exercises = new ArrayList<>();
            
            // Adjust base exercises intensity if keywords match
            for (PlanExercise baseEx : day.getExercises()) {
                String reps = baseEx.getRepRange();
                String effort = baseEx.getEffortNotes();
                
                if (normalizedNotes.contains("heavy") || normalizedNotes.contains("strength") || normalizedNotes.contains("power")) {
                    reps = "5 reps";
                    effort = "Heavy (RPE 8-9)";
                } else if (normalizedNotes.contains("light") || normalizedNotes.contains("endurance") || normalizedNotes.contains("pump")) {
                    reps = "15-20 reps";
                    effort = "Moderate (RPE 6-7)";
                }
                
                exercises.add(new PlanExercise(baseEx.getExerciseName(), baseEx.getSets(), reps, effort, baseEx.getOptionalNotes()));
            }

            // Add targeted accessories based on Day Number for variety
            int dayNum = day.getDayNumber();
            
            if (mentionsAny(normalizedNotes, PUSH_KEYWORDS)) {
                if (dayNum % 2 != 0) maybeAdd(exercises, exercise("Incline DB Press", 3, "10 reps", "Focus on upper chest", "Custom Focus"));
                else maybeAdd(exercises, exercise("Chest Flys", 3, "15 reps", "Controlled squeeze", "Custom Focus"));
            }
            if (mentionsAny(normalizedNotes, PULL_KEYWORDS)) {
                if (dayNum % 2 != 0) maybeAdd(exercises, exercise("Dumbbell Rows", 3, "10 reps", "Pull to hip", "Custom Focus"));
                else maybeAdd(exercises, exercise("Face Pulls", 3, "15 reps", "Pull to forehead", "Custom Focus"));
            }
            if (mentionsAny(normalizedNotes, LEGS_KEYWORDS)) {
                if (dayNum % 2 != 0) maybeAdd(exercises, exercise("Walking Lunges", 3, "10/side", "Keep torso upright", "Custom Focus"));
                else maybeAdd(exercises, exercise("Leg Extensions", 3, "15 reps", "Hold at top", "Custom Focus"));
            }
            if (mentionsAny(normalizedNotes, ARMS_KEYWORDS)) {
                if (dayNum % 2 != 0) maybeAdd(exercises, exercise("Bicep Curls", 3, "12 reps", "No swinging", "Custom Focus"));
                else maybeAdd(exercises, exercise("Tricep Pushdowns", 3, "12 reps", "Lock out elbows", "Custom Focus"));
            }
            if (mentionsAny(normalizedNotes, CORE_KEYWORDS)) {
                if (dayNum % 2 != 0) maybeAdd(exercises, exercise("Plank", 3, "60s", "Keep glutes tight", "Custom Focus"));
                else maybeAdd(exercises, exercise("Leg Raises", 3, "15 reps", "Lower slowly", "Custom Focus"));
            }

            personalizedDays.add(new PlanDay(day.getDayNumber(), day.getDayName(), day.getFocus(), exercises));
        }

        String description = template.getDescription() +
                " Specific goal notes were used only to add compatible accessory work without replacing the base split.";
        return new PlanTemplate(
                template.getGoalType(),
                template.getDaysPerWeek(),
                template.getSplitName(),
                description,
                personalizedDays
        );
    }

    private boolean mentionsAny(String notes, String... keywords) {
        for (String keyword : keywords) {
            if (notes.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void maybeAdd(List<PlanExercise> exercises, PlanExercise extra) {
        for (PlanExercise exercise : exercises) {
            if (exercise.getExerciseName().equalsIgnoreCase(extra.getExerciseName())) {
                return;
            }
        }
        exercises.add(extra);
    }

    private PlanExercise accessoryForPush(GoalType goalType) {
        switch (goalType) {
            case STRENGTH:
                return exercise("Chest Fly", 2, "10-12 reps", "Optional last set near failure", "Accessory only");
            case HYPERTROPHY:
                return exercise("Push-Up", 2, "12-20 reps", "Stop before form breaks", "Accessory only");
            default:
                return exercise("Machine Chest Press", 2, "15-20 reps", "Stop 2-3 reps before failure", "Keep rest short");
        }
    }

    private PlanExercise accessoryForPull(GoalType goalType) {
        switch (goalType) {
            case STRENGTH:
                return exercise("Face Pull", 2, "12-15 reps", "Stop 1-2 reps before failure", null);
            case HYPERTROPHY:
                return exercise("Face Pull", 2, "12-15 reps", "Optional last set near failure", "Accessory only");
            default:
                return exercise("Seated Row", 2, "15-20 reps", "Stop 2-3 reps before failure", "Keep rest short");
        }
    }

    private PlanExercise accessoryForLegs(GoalType goalType) {
        switch (goalType) {
            case STRENGTH:
                return exercise("Walking Lunge", 2, "8 reps per leg", "Stop 1-2 reps before failure", null);
            case HYPERTROPHY:
                return exercise("Leg Extension", 2, "12-15 reps", "Optional last set near failure", "Accessory only");
            default:
                return exercise("Step-Up", 2, "12 reps per leg", "Stop 2-3 reps before failure", "Keep rest short");
        }
    }

    private PlanExercise accessoryForArms(GoalType goalType) {
        switch (goalType) {
            case STRENGTH:
                return exercise("Hammer Curl", 2, "8-10 reps", "Stop 1-2 reps before failure", null);
            case HYPERTROPHY:
                return exercise("Hammer Curl", 2, "10-15 reps", "Optional last set near failure", "Accessory only");
            default:
                return exercise("Cable Curl", 2, "15-20 reps", "Stop 2-3 reps before failure", "Keep rest short");
        }
    }

    private PlanExercise accessoryForCore(GoalType goalType) {
        switch (goalType) {
            case STRENGTH:
                return exercise("Side Plank", 2, "30-45 seconds per side", "Move with control", null);
            case HYPERTROPHY:
                return exercise("Hanging Knee Raise", 2, "10-15 reps", "Move with control", null);
            default:
                return exercise("Plank", 2, "30-45 seconds", "Move with control", null);
        }
    }

    private PlanExercise accessoryForConditioning(GoalType goalType) {
        switch (goalType) {
            case STRENGTH:
                return exercise("Easy Bike or Walk", 1, "10-15 minutes", "Keep effort easy", "Recovery-focused conditioning");
            case HYPERTROPHY:
                return exercise("Moderate Cardio", 1, "10-15 minutes", "Easy to moderate pace", "Recovery-focused conditioning");
            default:
                return exercise("Cardio Intervals", 1, "6-8 rounds of 30s harder / 60s easy", "Stay smooth and controlled", null);
        }
    }

    private PlanExercise exercise(String name, int sets, String reps, String effort, String optionalNotes) {
        return new PlanExercise(name, sets, reps, effort, optionalNotes);
    }
}
