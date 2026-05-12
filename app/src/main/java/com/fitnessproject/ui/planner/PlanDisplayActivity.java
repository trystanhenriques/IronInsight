package com.fitnessproject.ui.planner;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fitnessproject.R;
import com.fitnessproject.core.data.DatabaseHelper;
import com.fitnessproject.core.data.model.Routine;
import com.fitnessproject.core.data.model.RoutineExercise;
import com.fitnessproject.core.planner.PlanDay;
import com.fitnessproject.core.planner.PlanExercise;
import com.fitnessproject.core.planner.PlanTemplate;
import com.fitnessproject.core.planner.WeeklyPlanService;
import com.fitnessproject.ui.common.BaseActivity;

import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlanDisplayActivity extends BaseActivity {
    private PlanTemplate currentTemplate;
    private final WeeklyPlanService weeklyPlanService = new WeeklyPlanService();
    private LinearLayout layoutPlanDays;
    private DatabaseHelper dbHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_display);
        dbHelper = new DatabaseHelper(this);

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

        Button btnSaveAsRoutine = findViewById(R.id.btnSaveAsRoutine);
        btnSaveAsRoutine.setOnClickListener(v -> promptSaveRoutine());
    }

    private void promptSaveRoutine() {
        if (currentTemplate == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save as Routine");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        String defaultName = currentTemplate.getGoalType() != null ? "My " + currentTemplate.getGoalType().getDisplayName() + " Plan" : "My Plan";
        input.setText(defaultName);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a routine name.", Toast.LENGTH_SHORT).show();
            } else {
                savePlanAsRoutine(name);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private int extractFirstNumber(String repRange) {
        if (repRange == null) return 0;
        String val = repRange.replaceAll("[^0-9]", " ").trim();
        if (val.isEmpty()) return 0;
        String[] parts = val.split("\\s+");
        try {
            return Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void savePlanAsRoutine(String routineNamePrefix) {
        executor.execute(() -> {
            boolean allSuccess = true;
            for (PlanDay day : currentTemplate.getDays()) {
                if (day.getExercises().isEmpty()) continue;

                String dayDescriptor = day.getFocus();
                if (dayDescriptor == null || dayDescriptor.trim().isEmpty()) {
                    dayDescriptor = day.getDayName();
                }
                String baseName = routineNamePrefix + " - " + dayDescriptor;

                String finalName = baseName;
                int suffix = 1;
                while (dbHelper.doesRoutineNameExist(finalName)) {
                    finalName = baseName + " (" + suffix + ")";
                    suffix++;
                }

                List<RoutineExercise> routineExercises = new ArrayList<>();
                int orderIdx = 0;
                for (PlanExercise pe : day.getExercises()) {
                    RoutineExercise re = new RoutineExercise();
                    re.setExerciseName(pe.getExerciseName());
                    re.setDefaultSets(pe.getSets());
                    re.setDefaultReps(extractFirstNumber(pe.getRepRange()));
                    re.setOrderIndex(orderIdx++);
                    routineExercises.add(re);
                }

                Routine routine = new Routine();
                routine.setName(finalName);
                routine.setSource("Plan");
                routine.setExercises(routineExercises);

                long result = dbHelper.saveRoutine(routine);
                if (result == -1) {
                    allSuccess = false;
                }
            }

            final boolean successFinished = allSuccess;
            handler.post(() -> {
                if (successFinished) {
                    Toast.makeText(PlanDisplayActivity.this, "Routines saved to My Routines!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PlanDisplayActivity.this, "Failed to save some routines.", Toast.LENGTH_SHORT).show();
                }
            });
        });
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
