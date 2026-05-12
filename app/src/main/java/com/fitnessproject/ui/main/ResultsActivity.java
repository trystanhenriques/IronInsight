package com.fitnessproject.ui.main;

import android.os.Bundle;
import android.view.View;
import com.fitnessproject.core.engine.RecommendationEngine;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import com.fitnessproject.ui.formcheck.FormCheckStartActivity;

import androidx.appcompat.app.AppCompatActivity;

import com.fitnessproject.R;
import com.fitnessproject.ui.common.BaseActivity;

import com.fitnessproject.core.data.DatabaseHelper;
import com.fitnessproject.core.data.DataLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ResultsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ArrayList<String> cues = getIntent().getStringArrayListExtra("cues");

        TextView txtCues = findViewById(R.id.txtCues);
        View emptyResultsState = findViewById(R.id.emptyResultsState);
        View feedbackCard = findViewById(R.id.cardFeedback);
        View tierCard = findViewById(R.id.cardTier);
        View nextStepsCard = findViewById(R.id.cardNextSteps);
        setupButtons();

        if (cues == null || cues.isEmpty()) {
            emptyResultsState.setVisibility(View.VISIBLE);
            feedbackCard.setVisibility(View.GONE);
            tierCard.setVisibility(View.GONE);
            nextStepsCard.setVisibility(View.GONE);
            return;
        }

        emptyResultsState.setVisibility(View.GONE);
        feedbackCard.setVisibility(View.VISIBLE);
        tierCard.setVisibility(View.VISIBLE);
        nextStepsCard.setVisibility(View.VISIBLE);

        StringBuilder sb = new StringBuilder();
        for (String cue : cues) {
            sb.append("• ").append(cue).append("\n\n");
        }
        txtCues.setText(sb.toString());

        updateTierProgress();
        displayNextSteps();
    }

    private void setupButtons() {
        Button btnAnotherExercise = findViewById(R.id.btnAnotherExercise);
        btnAnotherExercise.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, FormCheckStartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        Button btnBackToMain = findViewById(R.id.btnBackToMain);
        btnBackToMain.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void updateTierProgress() {
        DatabaseHelper db = new DatabaseHelper(this);
        int workoutCount = db.getWorkoutCount();
        
        ProgressBar pb = findViewById(R.id.progressTier);
        TextView txtTierName = findViewById(R.id.txtTierName);
        TextView txtTierProgress = findViewById(R.id.txtTierProgress);
        
        if (workoutCount < 5) {
            txtTierName.setText("Tier: Beginner Foundation");
            pb.setMax(5);
            pb.setProgress(workoutCount);
            txtTierProgress.setText((5 - workoutCount) + " workouts until Intermediate");
        } else if (workoutCount < 20) {
            txtTierName.setText("Tier: Intermediate Strength");
            pb.setMax(20);
            pb.setProgress(workoutCount);
            txtTierProgress.setText((20 - workoutCount) + " workouts until Advanced Performance");
        } else {
            txtTierName.setText("Tier: Advanced Performance");
            pb.setMax(100);
            pb.setProgress(100);
            txtTierProgress.setText("Top Tier Reached! Keep pushing your limits.");
        }
    }

    private void displayNextSteps() {
        String exerciseId = getIntent().getStringExtra("exerciseId");
        String choiceId = getIntent().getStringExtra("choiceId");

        RecommendationEngine engine = new RecommendationEngine();
        RecommendationEngine.Recommendation rec = engine.getRecommendation(this, exerciseId, choiceId);

        TextView txtNextSteps = findViewById(R.id.txtNextSteps);
        StringBuilder sb = new StringBuilder();
        sb.append(rec.title).append("\n");
        sb.append(rec.description).append("\n\n");
        
        for (String item : rec.items) {
            sb.append("• ").append(item).append("\n");
        }

        txtNextSteps.setText(sb.toString());
    }
}
