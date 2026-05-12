package com.fitnessproject.core.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import com.fitnessproject.core.data.model.UserSession;
import com.fitnessproject.core.session.SessionManager;

public class PreferenceUtils {
    private static final String PREFS_NAME = "FitnessPrefs";
    private static final String KEY_TEXT_SIZE = "text_size";

    public static Context updateContextWithTextSize(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        String scopedKey = getScopedKey(context, KEY_TEXT_SIZE);
        int textSizePref = prefs.getInt(scopedKey, 1); // 0: Small, 1: Medium, 2: Large

        float fontScale = 1.0f;
        if (textSizePref == 0) fontScale = 0.85f;
        else if (textSizePref == 2) fontScale = 1.3f; // Increased to 1.3 for more visibility

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.fontScale = fontScale;
        
        // On some versions, we also need to update the base resources directly
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        
        return context.createConfigurationContext(config);
    }

    public static String getScopedKey(Context context, String baseKey) {
        try {
            UserSession session = SessionManager.getInstance(context).getCurrentSession();
            if (session != null && !session.isGuest() && session.getUserId() != null) {
                return baseKey + "_user_" + session.getUserId();
            }
        } catch (Exception e) {
            // Fallback if session manager fails during early startup
        }
        return baseKey + "_guest";
    }
}
