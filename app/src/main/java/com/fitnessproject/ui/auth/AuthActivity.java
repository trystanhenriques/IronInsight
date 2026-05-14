package com.fitnessproject.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fitnessproject.R;
import com.fitnessproject.ui.common.BaseActivity;
import com.fitnessproject.ui.main.MainActivity;

/**
 * Entry Activity that bootstraps the authentication flow logic holding single shared AuthViewModel.
 * If the user successfully logs in, registers, or chooses guest mode, it ends and pops clear back
 * directly routing straight into MainActivity securely handling offline flow exclusively.
 */
public class AuthActivity extends BaseActivity {

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        AuthViewModelFactory factory = new AuthViewModelFactory(this);
        authViewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);

        // Preemptively check if someone is already logged in to skip this whole flow.
        // If a session exists, ensure disclaimer is accepted before routing to main.
        com.fitnessproject.core.session.SessionManager sessionManager =
            com.fitnessproject.core.session.SessionManager.getInstance(getApplicationContext());
        if (sessionManager.isLoggedIn()) {
            handleSuccessfulAuth();
            return;
        }

        // Listen to state changes from our single source of truth across Login/Register screens.
        authViewModel.getCurrentScreen().observe(this, screen -> {
            if (screen == AuthViewModel.AuthScreen.LOGIN) {
                replaceFragment(new LoginFragment());
            } else if (screen == AuthViewModel.AuthScreen.REGISTER) {
                replaceFragment(new RegisterFragment());
            }
        });

        // Watch for overall successful authentication changes so we know when to bail to the main app perfectly.
        authViewModel.getUiState().observe(this, state -> {
            if (state.getStatus() == AuthUiState.Status.SUCCESS ||
                state.getStatus() == AuthUiState.Status.GUEST_SUCCESS) {

                handleSuccessfulAuth();
            }
        });
    }

    private void handleSuccessfulAuth() {
        SharedPreferences prefs = getSharedPreferences("FitnessPrefs", Context.MODE_PRIVATE);
        String disclaimerKey = com.fitnessproject.core.util.PreferenceUtils.getScopedKey(this, "disclaimer_accepted");
        
        boolean accepted = prefs.getBoolean(disclaimerKey, false);
        
        if (accepted) {
            proceedToMain();
        } else {
            showMedicalDisclaimer(disclaimerKey);
        }
    }

    private void showMedicalDisclaimer(String disclaimerKey) {
        new AlertDialog.Builder(this)
                .setTitle("Medical Disclaimer")
                .setMessage("This application is a tool for general fitness guidance only. It is not a substitute for professional medical advice, diagnosis, or treatment. Always seek the advice of your physician or other qualified health provider with any questions you may have regarding a medical condition. Stop exercising immediately if you feel pain, dizziness, or shortness of breath.")
                .setCancelable(false)
                .setPositiveButton("I Acknowledge & Accept", (dialog, which) -> {
                    getSharedPreferences("FitnessPrefs", Context.MODE_PRIVATE)
                            .edit().putBoolean(disclaimerKey, true).apply();
                    proceedToMain();
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    // Reset auth state so they can try again or stay on login
                    authViewModel.resetState();
                    com.fitnessproject.core.session.SessionManager.getInstance(getApplicationContext()).logout();
                })
                .show();
    }

    private void proceedToMain() {
        authViewModel.resetState();
        goToMain();
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Close auth gracefully wiping traces out of backstack completely.
    }
}
