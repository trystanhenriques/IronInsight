package com.fitnessproject.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fitnessproject.R;
import com.fitnessproject.ui.common.BaseActivity;
import com.fitnessproject.core.data.DatabaseHelper;
import com.fitnessproject.core.data.model.UserSession;
import com.fitnessproject.core.session.SessionManager;
import com.fitnessproject.core.util.PreferenceUtils;
import com.fitnessproject.ui.auth.AuthActivity;

public class SettingsActivity extends BaseActivity {

    private static final String PREFS_NAME = "FitnessPrefs";
    private static final String KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted";
    private static final String KEY_TEXT_SIZE = "text_size";

    private SettingsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings & Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize ViewModel mapped natively to offline Room and SharedPreferences singleton Tracker
        viewModel = new ViewModelProvider(this, new SettingsViewModelFactory(this)).get(SettingsViewModel.class);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String scopedDisclaimerKey = PreferenceUtils.getScopedKey(this, KEY_DISCLAIMER_ACCEPTED);
        String scopedTextSizeKey = PreferenceUtils.getScopedKey(this, KEY_TEXT_SIZE);

        // Account UI Bindings
        TextView txtAccountStatus = findViewById(R.id.txtAccountStatus);
        Button btnSignOut = findViewById(R.id.btnSignOut);

        viewModel.getAccountStatusText().observe(this, txtAccountStatus::setText);

        viewModel.getIsGuestUser().observe(this, isGuest -> {
            if (isGuest) {
                btnSignOut.setText("Log In / Register");
                btnSignOut.setOnClickListener(v -> viewModel.onLoginRegisterClicked());
            } else {
                btnSignOut.setText("Sign Out");
                btnSignOut.setOnClickListener(v -> viewModel.onLogoutClicked());
            }
        });

        viewModel.getNavigateToAuthEvent().observe(this, navigate -> {
            if (navigate) {
                // Clear backstack heavily via FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK
                Intent intent = new Intent(this, AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                viewModel.onNavigatedToAuth();
                finish();
            }
        });

        // Disclaimer Checkbox
        CheckBox cbDisclaimer = findViewById(R.id.cbDisclaimer);
        cbDisclaimer.setChecked(prefs.getBoolean(scopedDisclaimerKey, false));
        cbDisclaimer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(scopedDisclaimerKey, isChecked).apply();
        });

        // Text Size RadioGroup
        RadioGroup rgTextSize = findViewById(R.id.rgTextSize);
        int savedTextSize = prefs.getInt(scopedTextSizeKey, 1); // 0: Small, 1: Normal, 2: Large
        if (savedTextSize == 0) rgTextSize.check(R.id.rbSmall);
        else if (savedTextSize == 2) rgTextSize.check(R.id.rbLarge);
        else rgTextSize.check(R.id.rbMedium);

        rgTextSize.setOnCheckedChangeListener((group, checkedId) -> {
            int size = 1;
            if (checkedId == R.id.rbSmall) size = 0;
            else if (checkedId == R.id.rbLarge) size = 2;
            prefs.edit().putInt(scopedTextSizeKey, size).apply();
            
            Toast.makeText(this, "Text size saved. Re-opening app to apply.", Toast.LENGTH_SHORT).show();
            recreate();
        });

        // Delete History
        Button btnDeleteHistory = findViewById(R.id.btnDeleteHistory);
        btnDeleteHistory.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete History")
                    .setMessage("Are you sure you want to delete all workout history? This cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        DatabaseHelper db = new DatabaseHelper(this);
                        db.deleteAllWorkouts();
                        Toast.makeText(this, "Workout history deleted for this account.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
