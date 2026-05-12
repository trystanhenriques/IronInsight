package com.fitnessproject.ui.planner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fitnessproject.R;
import com.fitnessproject.core.planner.PlanDay;
import com.fitnessproject.core.planner.PlanExercise;
import com.fitnessproject.core.planner.PlanTemplate;
import com.fitnessproject.core.planner.WeeklyPlanService;
import com.fitnessproject.ui.common.BaseActivity;

import androidx.core.content.ContextCompat;
import java.util.Locale;

public class PlanDisplayActivity extends BaseActivity {
    private PlanTemplate currentTemplate;
    private final WeeklyPlanService weeklyPlanService = new WeeklyPlanService();
    private LinearLayout layoutPlanDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_display);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Weekly Plan");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        currentTemplate = (PlanTemplate) getIntent().getSerializableExtra("plan_template");
        layoutPlanDays = findViewById(R.id.layoutPlanDays);

        if (currentTemplate != null) {
            updateUi();
        }

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateUi() {
        TextView txtPlanGoal = findViewById(R.id.txtPlanGoal);
        TextView txtPlanSplit = findViewById(R.id.txtPlanSplitName);
        TextView txtPlanDescription = findViewById(R.id.txtPlanDescription);

        txtPlanGoal.setText(currentTemplate.getGoalType().getDisplayName() + " Plan");
        txtPlanSplit.setText(currentTemplate.getSplitName());
        txtPlanDescription.setText(currentTemplate.getDescription());

        renderDays();
    }

    private void renderDays() {
        layoutPlanDays.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (PlanDay day : currentTemplate.getDays()) {
            View dayHeader = inflater.inflate(R.layout.item_plan_day_header, layoutPlanDays, false);
            TextView txtDayTitle = dayHeader.findViewById(R.id.txtDayTitle);
            TextView txtDayFocus = dayHeader.findViewById(R.id.txtDayFocus);

            txtDayTitle.setText(day.getDayName());
            txtDayFocus.setText(day.getFocus());
            layoutPlanDays.addView(dayHeader);

            for (PlanExercise exercise : day.getExercises()) {
                View exerciseView = inflater.inflate(R.layout.item_plan_exercise, layoutPlanDays, false);
                TextView txtExerciseName = exerciseView.findViewById(R.id.txtExerciseName);
                TextView txtExerciseDetails = exerciseView.findViewById(R.id.txtExerciseDetails);
                Button btnReplace = exerciseView.findViewById(R.id.btnReplace);
                Button btnRemove = exerciseView.findViewById(R.id.btnRemove);

                txtExerciseName.setText(exercise.getExerciseName());
                txtExerciseDetails.setText(String.format(Locale.US, "%d sets x %s | %s", 
                        exercise.getSets(), exercise.getRepRange(), exercise.getEffortNotes()));

                btnReplace.setOnClickListener(v -> {
                    currentTemplate = weeklyPlanService.replaceExercise(currentTemplate, day.getDayNumber(), exercise.getExerciseName());
                    renderDays();
                });

                btnRemove.setOnClickListener(v -> {
                    currentTemplate = weeklyPlanService.removeExercise(currentTemplate, day.getDayNumber(), exercise.getExerciseName());
                    renderDays();
                });

                layoutPlanDays.addView(exerciseView);
            }
        }

        addSafetyNotes(inflater);
    }

    private void addSafetyNotes(LayoutInflater inflater) {
        View safetyHeader = inflater.inflate(R.layout.item_plan_day_header, layoutPlanDays, false);
        ((TextView) safetyHeader.findViewById(R.id.txtDayTitle)).setText("Safety Notes");
        safetyHeader.findViewById(R.id.txtDayFocus).setVisibility(View.GONE);
        layoutPlanDays.addView(safetyHeader);

        TextView txtSafety = new TextView(this);
        txtSafety.setText("• Stop if you feel sharp, worsening, or persistent pain.\n" +
                "• Most sets should finish with 1-3 reps in reserve unless noted.\n" +
                "• Do not take heavy barbell lifts to true failure.");
        txtSafety.setTextColor(ContextCompat.getColor(this, R.color.fitness_text_secondary));
        txtSafety.setPadding(0, 0, 0, 32);
        layoutPlanDays.addView(txtSafety);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
