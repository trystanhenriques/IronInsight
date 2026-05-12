package com.fitnessproject.ui.formcheck;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.fitnessproject.R;
import com.fitnessproject.core.data.DataLoader;
import com.fitnessproject.ui.common.BaseActivity;
import com.fitnessproject.ui.workout.WorkoutQuestionsActivity;

import java.util.List;

public class FormCheckStartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_check_start);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Spinner spinner = findViewById(R.id.spinnerExercise);
        Button btnContinue = findViewById(R.id.btnContinue);

        List<String> exercises = DataLoader.getExerciseNames(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner,
                exercises
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinner.setAdapter(adapter);

        btnContinue.setOnClickListener(v -> {
            String selected = (String) spinner.getSelectedItem();
            String exerciseId = DataLoader.mapNameToId(this, selected);

            //Next screen
            Intent i = new Intent(FormCheckStartActivity.this, WorkoutQuestionsActivity.class);
            i.putExtra("exercise_id", exerciseId);
            startActivity(i);
        });
    }
}