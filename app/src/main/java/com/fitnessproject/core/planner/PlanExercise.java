package com.fitnessproject.core.planner;

import java.io.Serializable;

public class PlanExercise implements Serializable {
    private final String exerciseName;
    private final int sets;
    private final String repRange;
    private final String effortNotes;
    private final String optionalNotes;

    public PlanExercise(String exerciseName, int sets, String repRange, String effortNotes, String optionalNotes) {
        this.exerciseName = exerciseName;
        this.sets = sets;
        this.repRange = repRange;
        this.effortNotes = effortNotes;
        this.optionalNotes = optionalNotes;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public int getSets() {
        return sets;
    }

    public String getRepRange() {
        return repRange;
    }

    public String getEffortNotes() {
        return effortNotes;
    }

    public String getOptionalNotes() {
        return optionalNotes;
    }
}
