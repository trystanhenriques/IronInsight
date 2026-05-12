package com.fitnessproject.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.fitnessproject.core.session.SessionManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DatabaseHelperUserStatsTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("fitness_project.db");
        SessionManager.getInstance(context).clearSession();
        SessionManager.getInstance(context).startGuestSession();
    }

    @Test
    public void userStatsSummary_ComputesReliableWorkoutMetrics() {
        DatabaseHelper db = new DatabaseHelper(context);
        db.addWorkout("Bench Press", "185", "5", "3", "Chest, Triceps");
        db.addWorkout("Bench Press", "195", "4", "3", "Chest, Triceps");
        db.addWorkout("Deadlift", "315", "3", "2", "Back, Legs");

        DatabaseHelper.UserStatsSummary stats = db.getUserStatsSummary();

        assertTrue(stats.hasData);
        assertEquals(1, stats.totalWorkouts);
        assertEquals(8, stats.totalSets);
        assertEquals(33, stats.totalReps);
        assertEquals(2, stats.distinctExercises);
        assertTrue(stats.workoutsLast7Days >= 1);
        assertTrue(stats.workoutsLast7Days <= stats.totalWorkouts);
        assertTrue(stats.workoutsLast30Days >= stats.workoutsLast7Days);
        assertTrue(stats.activeDaysThisMonth >= 1);
        assertTrue(stats.averageWorkoutsPerWeek > 0d);
        assertTrue(stats.mostLoggedExerciseLabel.contains("Bench Press"));
        assertTrue(stats.heaviestSetLabel.contains("315"));
        assertTrue(stats.heaviestSetLabel.contains("Deadlift"));
        assertTrue(stats.favoriteCategoryLabel.contains("Chest"));
    }
}
