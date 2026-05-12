package com.fitnessproject.ui.workout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitnessproject.R;
import com.fitnessproject.ui.common.BaseActivity;
import com.fitnessproject.ui.history.WorkoutHistoryActivity;
import com.fitnessproject.ui.progress.ProgressActivity;
import com.fitnessproject.ui.stats.UserStatsActivity;

public class WorkoutTrackerActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_tracker);

        Button btnStartNew = findViewById(R.id.btnStartNewWorkout);
        Button btnProgress = findViewById(R.id.btnViewProgress);
        Button btnHistory = findViewById(R.id.btnWorkoutHistory);
        Button btnStats = findViewById(R.id.btnUserStats);

        btnStartNew.setOnClickListener(v ->
                startActivity(new Intent(this, NewWorkoutActivity.class)));

        btnProgress.setOnClickListener(v ->
                startActivity(new Intent(this, ProgressActivity.class)));

        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, WorkoutHistoryActivity.class)));

        btnStats.setOnClickListener(v ->
                startActivity(new Intent(this, UserStatsActivity.class)));
    }
}
