package com.fitnessproject.ui.common;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.fitnessproject.core.util.PreferenceUtils;

/**
 * Base activity that applies global preferences like text scaling.
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply text size preference to the context
        super.attachBaseContext(PreferenceUtils.updateContextWithTextSize(newBase));
    }
}
