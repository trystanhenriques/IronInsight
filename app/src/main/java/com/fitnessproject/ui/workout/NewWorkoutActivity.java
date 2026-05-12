package com.fitnessproject.ui.workout;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fitnessproject.R;
import com.fitnessproject.core.data.DataLoader;
import com.fitnessproject.core.data.DatabaseHelper;
import com.fitnessproject.ui.common.BaseActivity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NewWorkoutActivity extends BaseActivity {

    private static final String CUSTOM_EXERCISE_OPTION = "Custom Exercise...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_workout);

        Spinner spinner = findViewById(R.id.spinnerWorkoutExercise);
        EditText etCustomExercise = findViewById(R.id.etCustomExercise);
        View txtCustomExerciseLabel = findViewById(R.id.txtCustomExerciseLabel);
        Button btnSelectCategories = findViewById(R.id.btnSelectCategories);
        TextView txtSelectedCategories = findViewById(R.id.txtSelectedCategories);
        EditText etWeight = findViewById(R.id.etWeight);
        EditText etReps = findViewById(R.id.etReps);
        EditText etSets = findViewById(R.id.etSets);
        Button btnSave = findViewById(R.id.btnSaveWorkout);

        List<String> allCategories = DataLoader.getDefaultCategories();
        Set<String> selectedCategories = new LinkedHashSet<>();
        selectedCategories.add("Other");

        List<String> exerciseList = new ArrayList<>(DataLoader.getExerciseNames(this));
        exerciseList.add(CUSTOM_EXERCISE_OPTION);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, exerciseList);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinner.setAdapter(adapter);

        updateSelectedCategoriesLabel(txtSelectedCategories, selectedCategories);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = exerciseList.get(position);
                boolean showCustomInput = CUSTOM_EXERCISE_OPTION.equals(selected);
                txtCustomExerciseLabel.setVisibility(showCustomInput ? View.VISIBLE : View.GONE);
                etCustomExercise.setVisibility(showCustomInput ? View.VISIBLE : View.GONE);
                if (!showCustomInput) {
                    etCustomExercise.setText("");
                    selectedCategories.clear();
                    selectedCategories.addAll(DataLoader.getExerciseCategories(NewWorkoutActivity.this, selected));
                } else {
                    selectedCategories.clear();
                    selectedCategories.add("Other");
                }
                updateSelectedCategoriesLabel(txtSelectedCategories, selectedCategories);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                txtCustomExerciseLabel.setVisibility(View.GONE);
                etCustomExercise.setVisibility(View.GONE);
            }
        });

        btnSelectCategories.setOnClickListener(v -> showCategoryPickerDialog(
                allCategories,
                selectedCategories,
                txtSelectedCategories
        ));

        btnSave.setOnClickListener(v -> {
            String selectedExercise = (String) spinner.getSelectedItem();
            String customExercise = etCustomExercise.getText().toString().trim();
            String exercise = selectedExercise;
            String weight = etWeight.getText().toString().trim();
            String reps = etReps.getText().toString().trim();
            String sets = etSets.getText().toString().trim();

            if (CUSTOM_EXERCISE_OPTION.equals(selectedExercise)) {
                if (customExercise.isEmpty()) {
                    Toast.makeText(this, "Please enter a custom exercise name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                exercise = customExercise;
            }

            if (selectedCategories.isEmpty()) {
                Toast.makeText(this, "Please select at least one category.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (weight.isEmpty() || reps.isEmpty() || sets.isEmpty()) {
                Toast.makeText(this, "Please fill out weight, reps, and sets.", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper db = new DatabaseHelper(this);
            db.addWorkout(exercise, weight, reps, sets, DataLoader.toCategoryCsv(new ArrayList<>(selectedCategories)));

            Toast.makeText(this, "Workout Saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showCategoryPickerDialog(
            List<String> allCategories,
            Set<String> selectedCategories,
            TextView txtSelectedCategories
    ) {
        String[] categoryArray = allCategories.toArray(new String[0]);
        boolean[] checkedItems = new boolean[categoryArray.length];

        for (int i = 0; i < categoryArray.length; i++) {
            checkedItems[i] = selectedCategories.contains(categoryArray[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Categories")
                .setMultiChoiceItems(categoryArray, checkedItems, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        selectedCategories.add(categoryArray[which]);
                    } else {
                        selectedCategories.remove(categoryArray[which]);
                    }
                })
                .setPositiveButton("Done", (dialog, which) -> {
                    if (selectedCategories.isEmpty()) {
                        selectedCategories.add("Other");
                    }
                    updateSelectedCategoriesLabel(txtSelectedCategories, selectedCategories);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateSelectedCategoriesLabel(TextView label, Set<String> selectedCategories) {
        List<String> ordered = new ArrayList<>();
        List<String> defaults = DataLoader.getDefaultCategories();
        for (String option : defaults) {
            if (selectedCategories.contains(option)) {
                ordered.add(option);
            }
        }
        if (ordered.isEmpty()) {
            ordered.addAll(selectedCategories);
        }
        label.setText("Selected: " + String.join(", ", ordered));
    }
}
