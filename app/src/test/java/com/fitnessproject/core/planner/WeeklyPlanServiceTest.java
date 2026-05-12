package com.fitnessproject.core.planner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WeeklyPlanServiceTest {

    @Test
    public void strengthSevenDays_UsesRecoveryDayInsteadOfHeavySeventhLift() {
        PlanTemplate template = new PlanTemplateProvider().getTemplate(GoalType.STRENGTH, 7);

        assertEquals("6 Training Days + Recovery Day", template.getSplitName());
        assertEquals(7, template.getDays().size());
        assertEquals("Active Recovery", template.getDays().get(6).getFocus());
        assertTrue(template.getDescription().contains("active recovery") || template.getDescription().contains("recovery"));
    }

    @Test
    public void specificFocusNotes_AddCompatibleAccessoriesWithoutReplacingTemplate() {
        WeeklyPlanService service = new WeeklyPlanService();

        PlanTemplate personalized = service.buildPlan(GoalType.HYPERTROPHY, 3, "extra chest and arms focus");

        assertEquals(3, personalized.getDays().size());
        assertTrue(personalized.getDescription().contains("Specific goal notes"));
        assertTrue(containsExercise(personalized, "Incline DB Press") || containsExercise(personalized, "Bicep Curls"));
    }

    @Test
    public void formatter_IncludesSafetyNotesAndReadableExerciseDetails() {
        PlanTemplate template = new PlanTemplateProvider().getTemplate(GoalType.ENDURANCE, 2);
        String formatted = new PlanFormatter().formatPlan(template);

        assertTrue(formatted.contains("Safety Notes"));
        assertTrue(formatted.contains("Stop if you feel sharp, worsening, or persistent pain."));
        assertTrue(formatted.contains("Goblet Squat"));
    }

    @Test
    public void customGoal_BuildsCustomTemplateFromNotes() {
        WeeklyPlanService service = new WeeklyPlanService();

        PlanTemplate customPlan = service.buildPlan(GoalType.CUSTOM, 4, "prep for a 5k with extra core and leg work");

        assertEquals(GoalType.CUSTOM, customPlan.getGoalType());
        assertTrue(customPlan.getSplitName().contains("Custom Focus"));
        assertTrue(customPlan.getDescription().contains("endurance"));
        assertTrue(customPlan.getDescription().contains("core"));
        assertTrue(customPlan.getDescription().contains("legs"));
        assertTrue(containsExercise(customPlan, "Plank") || containsExercise(customPlan, "Step-Up"));
    }

    @Test
    public void customGoal_RecognizesBroaderNaturalLanguageKeywords() {
        WeeklyPlanService service = new WeeklyPlanService();

        PlanTemplate customPlan = service.buildPlan(
                GoalType.CUSTOM,
                3,
                "I want to get more athletic, improve work capacity, grow my upper chest, and bring up my glutes and obliques"
        );

        assertEquals(GoalType.CUSTOM, customPlan.getGoalType());
        assertTrue(customPlan.getDescription().contains("conditioning"));
        assertTrue(customPlan.getDescription().contains("chest"));
        assertTrue(customPlan.getDescription().contains("legs"));
        assertTrue(customPlan.getDescription().contains("core"));
    }

    private boolean containsExercise(PlanTemplate template, String exerciseName) {
        for (PlanDay day : template.getDays()) {
            for (PlanExercise exercise : day.getExercises()) {
                if (exercise.getExerciseName().equalsIgnoreCase(exerciseName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
