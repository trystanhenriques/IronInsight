package com.fitnessproject.ui.routines;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fitnessproject.R;
import com.fitnessproject.core.data.DatabaseHelper;
import com.fitnessproject.core.data.model.Routine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoutinesActivity extends AppCompatActivity {

    private ListView listRoutines;
    private View emptyStateRoutines;
    private DatabaseHelper dbHelper;
    private RoutineAdapter adapter;
    private List<Routine> routineList;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routines);

        dbHelper = new DatabaseHelper(this);
        routineList = new ArrayList<>();
        adapter = new RoutineAdapter(routineList);

        listRoutines = findViewById(R.id.listRoutines);
        emptyStateRoutines = findViewById(R.id.emptyStateRoutines);

        listRoutines.setAdapter(adapter);

        findViewById(R.id.btnEmptyCreateRoutine).setOnClickListener(v -> launchCreateRoutine());
        findViewById(R.id.fabCreateRoutine).setOnClickListener(v -> launchCreateRoutine());

        listRoutines.setOnItemLongClickListener((parent, view, position, id) -> {
            Routine routine = routineList.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Delete Routine")
                    .setMessage("Are you sure you want to delete '" + routine.getName() + "'?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteRoutine(routine))
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        listRoutines.setOnItemClickListener((parent, view, position, id) -> {
            Routine routine = routineList.get(position);
            StringBuilder sb = new StringBuilder();
            if (routine.getExercises() != null && !routine.getExercises().isEmpty()) {
                for (com.fitnessproject.core.data.model.RoutineExercise ex : routine.getExercises()) {
                    sb.append("• ").append(ex.getExerciseName())
                      .append(" (").append(ex.getDefaultSets()).append(" sets x ")
                      .append(ex.getDefaultReps()).append(" reps)\n");
                }
            } else {
                sb.append("No exercises found.");
            }

            new AlertDialog.Builder(this)
                    .setTitle(routine.getName())
                    .setMessage(sb.toString().trim())
                    .setPositiveButton("Close", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRoutines();
    }

    private void launchCreateRoutine() {
        Intent intent = new Intent(this, CreateRoutineActivity.class);
        startActivity(intent);
    }

    private void loadRoutines() {
        executor.execute(() -> {
            List<Routine> routines = dbHelper.getAllRoutines();
            handler.post(() -> {
                routineList.clear();
                routineList.addAll(routines);
                adapter.notifyDataSetChanged();

                if (routineList.isEmpty()) {
                    listRoutines.setVisibility(View.GONE);
                    emptyStateRoutines.setVisibility(View.VISIBLE);
                } else {
                    listRoutines.setVisibility(View.VISIBLE);
                    emptyStateRoutines.setVisibility(View.GONE);
                }
            });
        });
    }

    private void deleteRoutine(Routine routine) {
        executor.execute(() -> {
            dbHelper.deleteRoutine(routine.getId());
            handler.post(() -> {
                Toast.makeText(this, "Routine deleted", Toast.LENGTH_SHORT).show();
                loadRoutines();
            });
        });
    }

    private class RoutineAdapter extends ArrayAdapter<Routine> {
        public RoutineAdapter(List<Routine> routines) {
            super(RoutinesActivity.this, 0, routines);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_routine, parent, false);
            }

            Routine routine = getItem(position);
            TextView tvName = convertView.findViewById(R.id.tvRoutineName);
            TextView tvSource = convertView.findViewById(R.id.tvRoutineSource);

            if (routine != null) {
                tvName.setText(routine.getName());
                tvSource.setText(routine.getSource() != null ? routine.getSource() : "Custom");
            }

            return convertView;
        }
    }
}
