package com.fitnessproject.ui.stats;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fitnessproject.R;
import com.fitnessproject.core.data.DatabaseHelper;
import com.fitnessproject.ui.common.BaseActivity;
import com.fitnessproject.ui.workout.NewWorkoutActivity;

import java.util.Locale;

public class UserStatsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_stats);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DatabaseHelper db = new DatabaseHelper(this);
        DatabaseHelper.UserStatsSummary stats = db.getUserStatsSummary();

        bindStats(stats);

        Button btnEmptyStart = findViewById(R.id.btnStatsEmptyStart);
        btnEmptyStart.setOnClickListener(v ->
                startActivity(new Intent(this, NewWorkoutActivity.class)));
    }

    private void bindStats(DatabaseHelper.UserStatsSummary stats) {
        LinearLayout emptyState = findViewById(R.id.emptyStatsState);
        LinearLayout contentState = findViewById(R.id.statsContent);

        if (!stats.hasData) {
            emptyState.setVisibility(View.VISIBLE);
            contentState.setVisibility(View.GONE);
            return;
        }

        emptyState.setVisibility(View.GONE);
        contentState.setVisibility(View.VISIBLE);

        setText(R.id.txtStatsHeroValue, String.valueOf(stats.totalWorkouts));
        setText(R.id.txtStatsHeroLabel, stats.lastWorkoutDayLabel + " • " + stats.lastWorkoutDateLabel);
        bindTierProgress(stats.totalWorkouts);

        setText(R.id.txtTotalWorkoutsValue, String.valueOf(stats.totalWorkouts));
        setText(R.id.txtTotalVolumeValue, stats.totalVolumeLabel);
        setText(R.id.txtTotalSetsValue, String.valueOf(stats.totalSets));
        setText(R.id.txtTotalRepsValue, String.valueOf(stats.totalReps));
        setText(R.id.txtDistinctExercisesValue, String.valueOf(stats.distinctExercises));

        setText(R.id.txtLast7Value, String.valueOf(stats.workoutsLast7Days));
        setText(R.id.txtLast30Value, String.valueOf(stats.workoutsLast30Days));
        setText(R.id.txtAvgWeekValue, formatOneDecimal(stats.averageWorkoutsPerWeek));
        setText(R.id.txtActiveDaysValue, String.valueOf(stats.activeDaysThisMonth));

        setText(R.id.txtMostLoggedValue, stats.mostLoggedExerciseLabel);
        setText(R.id.txtHeaviestValue, stats.heaviestSetLabel);
        setText(R.id.txtFavoriteCategoryValue, stats.favoriteCategoryLabel);
    }

    private void bindTierProgress(int workoutCount) {
        ProgressBar progressBar = findViewById(R.id.progressStatsTier);
        TextView txtTierName = findViewById(R.id.txtStatsTierName);
        TextView txtTierProgress = findViewById(R.id.txtStatsTierProgress);

        if (workoutCount < 5) {
            txtTierName.setText("Tier: Beginner Foundation");
            progressBar.setMax(5);
            progressBar.setProgress(workoutCount);
            txtTierProgress.setText((5 - workoutCount) + " workouts until Intermediate");
        } else if (workoutCount < 20) {
            txtTierName.setText("Tier: Intermediate Strength");
            progressBar.setMax(20);
            progressBar.setProgress(workoutCount);
            txtTierProgress.setText((20 - workoutCount) + " workouts until Advanced Performance");
        } else {
            txtTierName.setText("Tier: Advanced Performance");
            progressBar.setMax(100);
            progressBar.setProgress(100);
            txtTierProgress.setText("Top Tier Reached! Keep pushing your limits.");
        }
    }

    private void setText(int viewId, String value) {
        TextView textView = findViewById(viewId);
        textView.setText(value);
    }

    private String formatOneDecimal(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
