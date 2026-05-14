package com.fitnessproject.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.fitnessproject.R;
import com.fitnessproject.core.util.PreferenceUtils;
import com.fitnessproject.ui.common.BaseActivity;
import com.fitnessproject.ui.formcheck.FormCheckFragment;
import com.fitnessproject.ui.planner.PlanFragment;
import com.fitnessproject.ui.settings.SettingsTabFragment;
import com.fitnessproject.ui.workout.WorkoutFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {

    private HomeFragment homeFragment;
    private FormCheckFragment formCheckFragment;
    private WorkoutFragment workoutFragment;
    private PlanFragment planFragment;
    private SettingsTabFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_home);

        setupFragments(savedInstanceState);
        setupBottomNav();
    }

    private void setupFragments(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            formCheckFragment = new FormCheckFragment();
            workoutFragment = new WorkoutFragment();
            planFragment = new PlanFragment();
            settingsFragment = new SettingsTabFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, homeFragment, "home")
                    .add(R.id.fragmentContainer, formCheckFragment, "formcheck")
                    .add(R.id.fragmentContainer, workoutFragment, "workout")
                    .add(R.id.fragmentContainer, planFragment, "plan")
                    .add(R.id.fragmentContainer, settingsFragment, "settings")
                    .hide(formCheckFragment)
                    .hide(workoutFragment)
                    .hide(planFragment)
                    .hide(settingsFragment)
                    .commit();
        } else {
            homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
            formCheckFragment = (FormCheckFragment) getSupportFragmentManager().findFragmentByTag("formcheck");
            workoutFragment = (WorkoutFragment) getSupportFragmentManager().findFragmentByTag("workout");
            planFragment = (PlanFragment) getSupportFragmentManager().findFragmentByTag("plan");
            settingsFragment = (SettingsTabFragment) getSupportFragmentManager().findFragmentByTag("settings");
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) showFragment(homeFragment);
            else if (id == R.id.nav_form_check) showFragment(formCheckFragment);
            else if (id == R.id.nav_workout) showFragment(workoutFragment);
            else if (id == R.id.nav_plan) showFragment(planFragment);
            else if (id == R.id.nav_settings) showFragment(settingsFragment);
            return true;
        });
    }

    private void showFragment(Fragment target) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        for (Fragment f : new Fragment[]{homeFragment, formCheckFragment, workoutFragment, planFragment, settingsFragment}) {
            if (f == target) tx.show(f);
            else tx.hide(f);
        }
        tx.commit();
    }
}
