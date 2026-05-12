package com.fitnessproject.ui.history;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.fitnessproject.R;
import com.fitnessproject.ui.common.BaseActivity;
import com.fitnessproject.core.data.DatabaseHelper;
import com.fitnessproject.ui.common.GroupedWorkoutListAdapter;

import java.util.List;

public class WorkoutHistoryActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        ListView listView = findViewById(R.id.listHistory);
        LinearLayout emptyState = findViewById(R.id.emptyHistoryState);

        DatabaseHelper db = new DatabaseHelper(this);
        List<String> items = db.getAllWorkoutsGroupedByDate();

        if (items.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            GroupedWorkoutListAdapter adapter = new GroupedWorkoutListAdapter(this, items);
            listView.setAdapter(adapter);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
