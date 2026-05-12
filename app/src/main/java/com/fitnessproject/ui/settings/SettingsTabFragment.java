package com.fitnessproject.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fitnessproject.R;
import com.fitnessproject.core.data.DatabaseHelper;
import com.fitnessproject.core.data.model.UserSession;
import com.fitnessproject.core.session.SessionManager;
import com.fitnessproject.core.util.PreferenceUtils;
import com.fitnessproject.ui.auth.AuthActivity;

public class SettingsTabFragment extends Fragment {

    private static final String PREFS_NAME = "FitnessPrefs";
    private static final String KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted";
    private static final String KEY_TEXT_SIZE = "text_size";

    private SettingsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, new SettingsViewModelFactory(requireContext()))
                .get(SettingsViewModel.class);

        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String scopedDisclaimerKey = PreferenceUtils.getScopedKey(requireContext(), KEY_DISCLAIMER_ACCEPTED);
        String scopedTextSizeKey = PreferenceUtils.getScopedKey(requireContext(), KEY_TEXT_SIZE);

        // Account section
        TextView txtAccountStatus = view.findViewById(R.id.txtAccountStatus);
        Button btnSignOut = view.findViewById(R.id.btnSignOut);

        viewModel.getAccountStatusText().observe(getViewLifecycleOwner(), txtAccountStatus::setText);

        viewModel.getIsGuestUser().observe(getViewLifecycleOwner(), isGuest -> {
            if (isGuest) {
                btnSignOut.setText("Log In / Register");
                btnSignOut.setOnClickListener(v -> viewModel.onLoginRegisterClicked());
            } else {
                btnSignOut.setText("Sign Out");
                btnSignOut.setOnClickListener(v -> viewModel.onLogoutClicked());
            }
        });

        viewModel.getNavigateToAuthEvent().observe(getViewLifecycleOwner(), navigate -> {
            if (Boolean.TRUE.equals(navigate)) {
                Intent intent = new Intent(requireActivity(), AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                viewModel.onNavigatedToAuth();
            }
        });

        // Disclaimer
        CheckBox cbDisclaimer = view.findViewById(R.id.cbDisclaimer);
        cbDisclaimer.setChecked(prefs.getBoolean(scopedDisclaimerKey, false));
        cbDisclaimer.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean(scopedDisclaimerKey, isChecked).apply());

        // Text size
        RadioGroup rgTextSize = view.findViewById(R.id.rgTextSize);
        int savedTextSize = prefs.getInt(scopedTextSizeKey, 1);
        if (savedTextSize == 0) rgTextSize.check(R.id.rbSmall);
        else if (savedTextSize == 2) rgTextSize.check(R.id.rbLarge);
        else rgTextSize.check(R.id.rbMedium);

        rgTextSize.setOnCheckedChangeListener((group, checkedId) -> {
            int size = 1;
            if (checkedId == R.id.rbSmall) size = 0;
            else if (checkedId == R.id.rbLarge) size = 2;
            prefs.edit().putInt(scopedTextSizeKey, size).apply();
            
            Toast.makeText(requireContext(),
                    "Text size saved. Re-opening app to apply.", Toast.LENGTH_SHORT).show();
            
            // Re-trigger the activity to show changes immediately if possible
            if (getActivity() != null) {
                getActivity().recreate();
            }
        });

        // Delete history
        view.findViewById(R.id.btnDeleteHistory).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete History")
                        .setMessage("Are you sure? This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            new DatabaseHelper(requireContext()).deleteAllWorkouts();
                            Toast.makeText(requireContext(),
                                    "Workout history deleted.", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show());
    }

}
