package com.fitnessproject.core.data.model;

import java.io.Serializable;

public class RoutineExercise implements Serializable {
    private String exerciseName;
    private int defaultSets;
    private int defaultReps;
    private int orderIndex;

    public RoutineExercise() {}

    public RoutineExercise(String exerciseName, int defaultSets, int defaultReps, int orderIndex) {
        this.exerciseName = exerciseName;
        this.defaultSets = defaultSets;
        this.defaultReps = defaultReps;
        this.orderIndex = orderIndex;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public int getDefaultSets() {
        return defaultSets;
    }

    public void setDefaultSets(int defaultSets) {
        this.defaultSets = defaultSets;
    }

    public int getDefaultReps() {
        return defaultReps;
    }

    public void setDefaultReps(int defaultReps) {
        this.defaultReps = defaultReps;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}

