package com.fitnessproject.core.planner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlanTemplate implements Serializable {
    private final GoalType goalType;
    private final int daysPerWeek;
    private final String splitName;
    private final String description;
    private final List<PlanDay> days;

    public PlanTemplate(GoalType goalType, int daysPerWeek, String splitName, String description, List<PlanDay> days) {
        this.goalType = goalType;
        this.daysPerWeek = daysPerWeek;
        this.splitName = splitName;
        this.description = description;
        this.days = new ArrayList<>(days);
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public int getDaysPerWeek() {
        return daysPerWeek;
    }

    public String getSplitName() {
        return splitName;
    }

    public String getDescription() {
        return description;
    }

    public List<PlanDay> getDays() {
        return Collections.unmodifiableList(days);
    }
}
