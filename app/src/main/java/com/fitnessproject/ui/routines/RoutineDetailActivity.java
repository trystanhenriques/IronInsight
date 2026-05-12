package com.fitnessproject.ui.routines;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.fitnessproject.R;
import com.fitnessproject.core.data.model.Routine;
import com.fitnessproject.core.data.model.RoutineExercise;
import com.fitnessproject.ui.common.BaseActivity;

import java.util.Locale;

public class RoutineDetailActivity extends BaseActivity {

    private Routine routine;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Routine Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        routine = (Routine) getIntent().getSerializableExtra("routine_data");

        if (routine == null) {
            Toast.makeText(this, "Error loading routine", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUi();
    }

    private void setupUi() {
        TextView txtName = findViewById(R.id.txtRoutineName);
        TextView txtSource = findViewById(R.id.txtRoutineSource);
        LinearLayout layoutExercises = findViewById(R.id.layoutExercises);

        txtName.setText(routine.getName());
        txtSource.setText("Source: " + (routine.getSource() != null ? routine.getSource() : "Custom"));

        layoutExercises.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (routine.getExercises() != null) {
            for (RoutineExercise ex : routine.getExercises()) {
                View exView = inflater.inflate(R.layout.item_routine_exercise_detail, layoutExercises, false);
                TextView txtExName = exView.findViewById(R.id.tvExerciseName);
                TextView txtExDetails = exView.findViewById(R.id.tvExerciseDetails);

                txtExName.setText(ex.getExerciseName());
                txtExDetails.setText(String.format(Locale.US, "%d sets x %d reps", ex.getDefaultSets(), ex.getDefaultReps()));

                layoutExercises.addView(exView);
            }
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnStartWorkout).setOnClickListener(v -> {
            // For now just show a toast, but this could launch a tracker pre-filled with this routine
            Toast.makeText(this, "Starting " + routine.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
