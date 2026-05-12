package com.fitnessproject.core.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Routine implements Serializable {
    private long id;
    private String name;
    private String source; // "Preset", "Plan", "Custom"
    private List<RoutineExercise> exercises;

    public Routine() {
        this.exercises = new ArrayList<>();
    }

    public Routine(long id, String name, String source, List<RoutineExercise> exercises) {
        this.id = id;
        this.name = name;
        this.source = source;
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<RoutineExercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<RoutineExercise> exercises) {
        this.exercises = exercises;
    }
}

