package com.fitnessproject.ui.workout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitnessproject.R;
import com.fitnessproject.ui.common.BaseActivity;
import com.fitnessproject.core.engine.FormCheckEngine;
import com.fitnessproject.core.model.Answer;
import com.fitnessproject.core.model.EvaluationResult;
import com.fitnessproject.ui.main.ResultsActivity;

import com.fitnessproject.core.data.DataLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkoutQuestionsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_questions);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String incomingExerciseId = getIntent().getStringExtra("exercise_id");
        final String exerciseId = (incomingExerciseId == null) ? "bench" : incomingExerciseId;

        RadioGroup rg = findViewById(R.id.rgChoices);
        Button btnComplete = findViewById(R.id.btnComplete);

        JSONObject questionObj = DataLoader.getQuestionsForExercise(this, exerciseId);
        if (questionObj == null) {
            Toast.makeText(this, "No questions found for this exercise.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            String questionId = questionObj.getString("id");
            JSONArray options = questionObj.getJSONArray("options");

            for (int i = 0; i < options.length(); i++) {
                JSONObject option = options.getJSONObject(i);
                RadioButton rb = new RadioButton(this);
                rb.setText(option.getString("text"));
                rb.setTextColor(getResources().getColor(R.color.fitness_text_primary));
                rb.setTag(option.getString("id"));
                rb.setId(i + 100); // Unique IDs
                rg.addView(rb);
            }

            btnComplete.setOnClickListener(v -> {
                int checkedId = rg.getCheckedRadioButtonId();
                EditText etDescription = findViewById(R.id.etUserDescription);
                String userDescription = etDescription.getText().toString().trim();

                if (checkedId == -1 && userDescription.isEmpty()) {
                    Toast.makeText(this, "Please select an option or describe how your set felt.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String choiceId = null;
                List<Answer> answers = new ArrayList<>();
                if (checkedId != -1) {
                    RadioButton selectedRb = findViewById(checkedId);
                    choiceId = (String) selectedRb.getTag();
                    answers.add(new Answer(questionId, choiceId));
                }

                // Keyword scanning: prioritize text-based keywords
                if (!userDescription.isEmpty()) {
                    String desc = userDescription.toLowerCase();
                    if (desc.contains("back") || desc.contains("spine")) choiceId = "lower_back";
                    else if (desc.contains("knee")) choiceId = "knees";
                    else if (desc.contains("shoulder")) choiceId = "shoulders";
                    else if (desc.contains("wrist")) choiceId = "wrists";
                    else if (desc.contains("grip")) choiceId = "grip";
                    else if (desc.contains("elbow")) choiceId = "elbows";
                }

                FormCheckEngine engine = new FormCheckEngine();
                EvaluationResult result = engine.evaluate(
                        this,
                        exerciseId,
                        answers,
                        userDescription
                );

                Intent i = new Intent(WorkoutQuestionsActivity.this, ResultsActivity.class);
                i.putStringArrayListExtra("cues", new ArrayList<>(result.getCueIds()));
                i.putExtra("exerciseId", exerciseId);
                i.putExtra("choiceId", choiceId);
                startActivity(i);
            });
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading questions.", Toast.LENGTH_SHORT).show();
        }
    }
}
