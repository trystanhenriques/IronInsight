package com.fitnessproject.ui.formcheck;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fitnessproject.R;
import com.fitnessproject.core.data.DataLoader;
import com.fitnessproject.ui.workout.WorkoutQuestionsActivity;

import java.util.List;

public class FormCheckFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_form_check_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinner = view.findViewById(R.id.spinnerExercise);
        Button btnContinue = view.findViewById(R.id.btnContinue);

        List<String> exercises = DataLoader.getExerciseNames(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_spinner, exercises);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinner.setAdapter(adapter);

        btnContinue.setOnClickListener(v -> {
            String selected = (String) spinner.getSelectedItem();
            String exerciseId = DataLoader.mapNameToId(requireContext(), selected);
            Intent i = new Intent(requireActivity(), WorkoutQuestionsActivity.class);
            i.putExtra("exercise_id", exerciseId);
            startActivity(i);
        });
    }
}
