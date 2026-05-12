package com.fitnessproject.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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
        // If a session (guest or user) exists, immediately route to main.
        com.fitnessproject.core.session.SessionManager sessionManager =
            com.fitnessproject.core.session.SessionManager.getInstance(getApplicationContext());
        if (sessionManager.isLoggedIn()) {
            goToMain();
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

                authViewModel.resetState();
                goToMain();
            }
        });
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
