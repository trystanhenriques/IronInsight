package com.fitnessproject.core.planner;

public class WeeklyPlanService {
    private final PlanTemplateProvider templateProvider;
    private final PlanPersonalizationEngine personalizationEngine;
    private final CustomGoalPlanGenerator customGoalPlanGenerator;

    public WeeklyPlanService() {
        this(new PlanTemplateProvider(), new PlanPersonalizationEngine());
    }

    public WeeklyPlanService(PlanTemplateProvider templateProvider, PlanPersonalizationEngine personalizationEngine) {
        this.templateProvider = templateProvider;
        this.personalizationEngine = personalizationEngine;
        this.customGoalPlanGenerator = new CustomGoalPlanGenerator(templateProvider, personalizationEngine);
    }

    public PlanTemplate buildPlan(GoalType goalType, int daysPerWeek, String specificNotes) {
        if (goalType == GoalType.CUSTOM) {
            return customGoalPlanGenerator.buildCustomPlan(daysPerWeek, specificNotes);
        }
        PlanTemplate baseTemplate = templateProvider.getTemplate(goalType, daysPerWeek);
        return personalizationEngine.applySpecificGoalNotes(baseTemplate, specificNotes);
    }

    public PlanTemplate removeExercise(PlanTemplate template, int dayNumber, String exerciseName) {
        return personalizationEngine.removeExercise(template, dayNumber, exerciseName);
    }

    public PlanTemplate replaceExercise(PlanTemplate template, int dayNumber, String exerciseToReplace) {
        return personalizationEngine.replaceExercise(template, dayNumber, exerciseToReplace);
    }
}
