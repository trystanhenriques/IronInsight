package com.fitnessproject.ui.routines;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitnessproject.R;
import com.fitnessproject.core.data.DataLoader;
import com.fitnessproject.core.data.DatabaseHelper;
import com.fitnessproject.core.data.model.Routine;
import com.fitnessproject.core.data.model.RoutineExercise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateRoutineActivity extends AppCompatActivity {

    private EditText etRoutineName;
    private LinearLayout containerExercises;
    private DatabaseHelper dbHelper;
    private List<String> exerciseNames;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_routine);

        dbHelper = new DatabaseHelper(this);
        exerciseNames = DataLoader.getExerciseNames(this);

        etRoutineName = findViewById(R.id.etRoutineName);
        containerExercises = findViewById(R.id.containerExercises);

        findViewById(R.id.btnAddExercise).setOnClickListener(v -> showAddExerciseDialog());
        findViewById(R.id.btnSaveRoutine).setOnClickListener(v -> saveRoutine());
    }

    private void showAddExerciseDialog() {
        if (exerciseNames == null || exerciseNames.isEmpty()) {
            Toast.makeText(this, "No exercises available to add.", Toast.LENGTH_SHORT).show();
            return;
        }

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, exerciseNames);
        spinner.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Select Exercise")
                .setView(spinner)
                .setPositiveButton("Add", (dialog, which) -> {
                    String selected = (String) spinner.getSelectedItem();
                    if (selected != null) {
                        addExerciseRow(selected);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addExerciseRow(String exerciseName) {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_routine_exercise_row, containerExercises, false);

        TextView tvName = rowView.findViewById(R.id.tvRowExerciseName);
        tvName.setText(exerciseName);

        rowView.findViewById(R.id.btnRemoveRow).setOnClickListener(v -> containerExercises.removeView(rowView));

        containerExercises.addView(rowView);
    }

    private void saveRoutine() {
        String name = etRoutineName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a routine name", Toast.LENGTH_SHORT).show();
            return;
        }

        int childCount = containerExercises.getChildCount();
        if (childCount == 0) {
            Toast.makeText(this, "Please add at least one exercise", Toast.LENGTH_SHORT).show();
            return;
        }

        List<RoutineExercise> exercises = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View rowView = containerExercises.getChildAt(i);

            TextView tvName = rowView.findViewById(R.id.tvRowExerciseName);
            EditText etSets = rowView.findViewById(R.id.etRowSets);
            EditText etReps = rowView.findViewById(R.id.etRowReps);

            String exerciseName = tvName.getText().toString();
            String setsStr = etSets.getText().toString();
            String repsStr = etReps.getText().toString();

            int sets = setsStr.isEmpty() ? 0 : Integer.parseInt(setsStr);
            int reps = repsStr.isEmpty() ? 0 : Integer.parseInt(repsStr);

            exercises.add(new RoutineExercise(exerciseName, sets, reps, i));
        }

        Routine routine = new Routine();
        routine.setName(name);
        routine.setSource("Custom");
        routine.setExercises(exercises);

        executor.execute(() -> {
            long id = dbHelper.saveRoutine(routine);
            handler.post(() -> {
                if (id != -1) {
                    Toast.makeText(CreateRoutineActivity.this, "Routine saved!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreateRoutineActivity.this, "Failed to save routine", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

