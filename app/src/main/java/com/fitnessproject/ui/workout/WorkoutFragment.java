package com.fitnessproject.ui.workout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fitnessproject.R;
import com.fitnessproject.ui.history.WorkoutHistoryActivity;
import com.fitnessproject.ui.progress.ProgressActivity;
import com.fitnessproject.ui.stats.UserStatsActivity;

public class WorkoutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_workout_tracker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnStartNewWorkout).setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), NewWorkoutActivity.class)));

        view.findViewById(R.id.btnViewProgress).setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), ProgressActivity.class)));

        view.findViewById(R.id.btnWorkoutHistory).setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), WorkoutHistoryActivity.class)));

        view.findViewById(R.id.btnUserStats).setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), UserStatsActivity.class)));
    }
}
