package com.fitnessproject.ui.planner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.fitnessproject.R;
import com.fitnessproject.core.planner.GoalType;
import com.fitnessproject.core.planner.PlanTemplate;
import com.fitnessproject.core.planner.WeeklyPlanService;
import com.fitnessproject.ui.common.BaseActivity;

public class PlanBuilderActivity extends BaseActivity {
    private final WeeklyPlanService weeklyPlanService = new WeeklyPlanService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_builder);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Weekly Plan Builder");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Spinner spinnerGoal = findViewById(R.id.spinnerGoal);
        String[] goals = {"Strength", "Hypertrophy", "Endurance", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, goals);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerGoal.setAdapter(adapter);

        EditText etGoals = findViewById(R.id.etSpecificGoals);
        Button btnGenerate = findViewById(R.id.btnGeneratePlan);

        // Define all radio button IDs
        int[] rbIds = {R.id.rb2, R.id.rb3, R.id.rb4, R.id.rb5, R.id.rb6, R.id.rb7};

        // Manually handle mutual exclusivity because of nested Layouts in RadioGroup
        for (int id : rbIds) {
            RadioButton rb = findViewById(id);
            rb.setOnClickListener(view -> {
                for (int otherId : rbIds) {
                    if (otherId != id) {
                        ((RadioButton) findViewById(otherId)).setChecked(false);
                    }
                }
            });
        }

        btnGenerate.setOnClickListener(v -> {
            String days = "3"; // Default
            for (int id : rbIds) {
                RadioButton rb = findViewById(id);
                if (rb.isChecked()) {
                    days = rb.getText().toString();
                    break;
                }
            }

            GoalType goalType = GoalType.fromDisplayName(spinnerGoal.getSelectedItem().toString());
            String specificNotes = etGoals.getText().toString().trim();
            PlanTemplate planTemplate = weeklyPlanService.buildPlan(goalType, Integer.parseInt(days), specificNotes);
            launchPlanDisplay(planTemplate);

            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });
    }

    private void launchPlanDisplay(PlanTemplate planTemplate) {
        Intent intent = new Intent(this, PlanDisplayActivity.class);
        intent.putExtra("plan_template", planTemplate);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
