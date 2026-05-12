package com.fitnessproject.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fitnessproject.R;
import com.fitnessproject.core.data.DatabaseHelper;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private TextView txtGreeting;
    private TextView txtStatThisWeek, txtStatVolume;
    private TextView txtTierName, txtTierProgress;
    private ProgressBar progressTier;
    private TextView txtLastSessionDate, txtLastSessionEmpty;
    private LinearLayout containerLastSession;
    private TextView txtHeaviest, txtFavoriteCategory;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        loadStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    private void bindViews(View view) {
        txtGreeting = view.findViewById(R.id.txtGreeting);
        txtStatThisWeek = view.findViewById(R.id.txtStatThisWeek);
        txtStatVolume = view.findViewById(R.id.txtStatVolume);
        txtTierName = view.findViewById(R.id.txtTierName);
        txtTierProgress = view.findViewById(R.id.txtTierProgress);
        progressTier = view.findViewById(R.id.progressTier);
        txtLastSessionDate = view.findViewById(R.id.txtLastSessionDate);
        txtLastSessionEmpty = view.findViewById(R.id.txtLastSessionEmpty);
        containerLastSession = view.findViewById(R.id.containerLastSession);
        txtHeaviest = view.findViewById(R.id.txtHeaviest);
        txtFavoriteCategory = view.findViewById(R.id.txtFavoriteCategory);
    }

    private void loadStats() {
        Context appCtx = requireContext().getApplicationContext();
        executor.execute(() -> {
            DatabaseHelper db = new DatabaseHelper(appCtx);
            DatabaseHelper.UserStatsSummary stats = db.getUserStatsSummary();
            List<DatabaseHelper.LastSessionEntry> lastSession = db.getLastSessionEntries();
            mainHandler.post(() -> {
                if (isAdded()) bindStats(stats, lastSession);
            });
        });
    }

    private void bindStats(DatabaseHelper.UserStatsSummary stats,
                           List<DatabaseHelper.LastSessionEntry> lastSession) {
        // Time-based greeting
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) txtGreeting.setText("Good morning");
        else if (hour < 17) txtGreeting.setText("Good afternoon");
        else txtGreeting.setText("Good evening");

        // Stat tiles
        txtStatThisWeek.setText(String.valueOf(stats.workoutsLast7Days));
        txtStatVolume.setText(stats.totalVolumeLabel);

        // Tier
        bindTier(stats.totalWorkouts);

        // Last session
        bindLastSession(stats, lastSession);

        // Highlights
        txtHeaviest.setText(stats.hasData ? stats.heaviestSetLabel : "—");
        txtFavoriteCategory.setText(stats.hasData ? stats.favoriteCategoryLabel : "—");
    }

    private void bindTier(int count) {
        if (count < 5) {
            txtTierName.setText("Beginner Foundation");
            progressTier.setMax(5);
            progressTier.setProgress(count);
            int left = 5 - count;
            txtTierProgress.setText(left + " workout" + (left == 1 ? "" : "s") + " to Intermediate");
        } else if (count < 20) {
            txtTierName.setText("Intermediate Strength");
            progressTier.setMax(20);
            progressTier.setProgress(count);
            int left = 20 - count;
            txtTierProgress.setText(left + " workout" + (left == 1 ? "" : "s") + " to Advanced");
        } else {
            txtTierName.setText("Advanced Performance");
            progressTier.setMax(100);
            progressTier.setProgress(100);
            txtTierProgress.setText("Top tier reached. Keep pushing.");
        }
    }

    private void bindLastSession(DatabaseHelper.UserStatsSummary stats,
                                 List<DatabaseHelper.LastSessionEntry> entries) {
        // Remove all previous dynamic rows, keeping only the empty-state TextView
        containerLastSession.removeAllViews();
        containerLastSession.addView(txtLastSessionEmpty);

        if (!stats.hasData || entries.isEmpty()) {
            txtLastSessionDate.setText("");
            txtLastSessionEmpty.setVisibility(View.VISIBLE);
            return;
        }

        txtLastSessionDate.setText(stats.lastWorkoutDayLabel);
        txtLastSessionEmpty.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int limit = Math.min(entries.size(), 5);
        for (int i = 0; i < limit; i++) {
            DatabaseHelper.LastSessionEntry entry = entries.get(i);

            if (i > 0) {
                View divider = new View(requireContext());
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                int gap = (int) (8 * getResources().getDisplayMetrics().density);
                divParams.topMargin = gap;
                divParams.bottomMargin = gap;
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(requireContext().getColor(R.color.fitness_divider_soft));
                containerLastSession.addView(divider);
            }

            TextView txtExercise = new TextView(requireContext());
            txtExercise.setTextAppearance(R.style.TextAppearanceFitnessCardTitle);
            txtExercise.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18);
            txtExercise.setText(entry.exercise);

            TextView txtDetail = new TextView(requireContext());
            txtDetail.setTextAppearance(R.style.TextAppearanceFitnessCaption);
            int topMargin = (int) (2 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            detailParams.topMargin = topMargin;
            txtDetail.setLayoutParams(detailParams);
            txtDetail.setText(formatSetDetail(entry));

            containerLastSession.addView(txtExercise);
            containerLastSession.addView(txtDetail);
        }
    }

    private String formatSetDetail(DatabaseHelper.LastSessionEntry entry) {
        StringBuilder sb = new StringBuilder();
        if (entry.weight != null && !entry.weight.trim().isEmpty() && !entry.weight.equals("0")) {
            sb.append(entry.weight).append(" lbs");
        }
        if (entry.reps != null && !entry.reps.trim().isEmpty() && !entry.reps.equals("0")) {
            if (sb.length() > 0) sb.append(" × ");
            sb.append(entry.reps).append(" reps");
        }
        if (entry.sets != null && !entry.sets.trim().isEmpty() && !entry.sets.equals("0")) {
            sb.append("  (").append(entry.sets);
            sb.append(entry.sets.equals("1") ? " set)" : " sets)");
        }
        return sb.length() > 0 ? sb.toString() : "—";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
