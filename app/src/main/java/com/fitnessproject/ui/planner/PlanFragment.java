package com.fitnessproject.ui.planner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fitnessproject.R;
import com.fitnessproject.core.planner.GoalType;
import com.fitnessproject.core.planner.PlanTemplate;
import com.fitnessproject.core.planner.WeeklyPlanService;

public class PlanFragment extends Fragment {

    private final WeeklyPlanService weeklyPlanService = new WeeklyPlanService();
    private static final int[] RB_IDS = {R.id.rb2, R.id.rb3, R.id.rb4, R.id.rb5, R.id.rb6, R.id.rb7};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_plan_builder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinnerGoal = view.findViewById(R.id.spinnerGoal);
        String[] goals = {"Strength", "Hypertrophy", "Endurance", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_spinner, goals);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerGoal.setAdapter(adapter);

        EditText etGoals = view.findViewById(R.id.etSpecificGoals);

        for (int id : RB_IDS) {
            RadioButton rb = view.findViewById(id);
            rb.setOnClickListener(clicked -> {
                for (int otherId : RB_IDS) {
                    if (otherId != id) {
                        ((RadioButton) view.findViewById(otherId)).setChecked(false);
                    }
                }
            });
        }

        view.findViewById(R.id.btnGeneratePlan).setOnClickListener(v -> {
            String days = "3";
            for (int id : RB_IDS) {
                RadioButton rb = view.findViewById(id);
                if (rb.isChecked()) {
                    days = rb.getText().toString();
                    break;
                }
            }

            GoalType goalType = GoalType.fromDisplayName(spinnerGoal.getSelectedItem().toString());
            String notes = etGoals.getText().toString().trim();
            PlanTemplate plan = weeklyPlanService.buildPlan(goalType, Integer.parseInt(days), notes);

            Intent intent = new Intent(requireActivity(), PlanDisplayActivity.class);
            intent.putExtra("plan_template", plan);
            startActivity(intent);

            InputMethodManager imm = (InputMethodManager)
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            View focus = requireActivity().getCurrentFocus();
            if (imm != null && focus != null) {
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
        });
    }
}
