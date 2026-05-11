package com.fitnessproject.core.planner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlanDay implements Serializable {
    private final int dayNumber;
    private final String dayName;
    private final String focus;
    private final List<PlanExercise> exercises;

    public PlanDay(int dayNumber, String dayName, String focus, List<PlanExercise> exercises) {
        this.dayNumber = dayNumber;
        this.dayName = dayName;
        this.focus = focus;
        this.exercises = new ArrayList<>(exercises);
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public String getDayName() {
        return dayName;
    }

    public String getFocus() {
        return focus;
    }

    public List<PlanExercise> getExercises() {
        return Collections.unmodifiableList(exercises);
    }
}
