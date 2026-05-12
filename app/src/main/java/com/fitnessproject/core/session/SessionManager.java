package com.fitnessproject.core.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.fitnessproject.core.data.model.UserAccount;
import com.fitnessproject.core.data.model.UserSession;

/**
 * Singleton managing active session storage using SharedPreferences.
 * Determines if a user (or guest) is actively currently logged-in between app launches.
 */
public class SessionManager {

    private static final String PREF_NAME = "fitness_session_prefs";
    private static final String KEY_IS_GUEST = "is_guest";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_HAS_SESSION = "has_session";

    private final SharedPreferences prefs;

    // Singleton backing field
    private static volatile SessionManager instance;

    private SessionManager(Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext != null) {
            prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        } else {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Restores the session stored locally if any. Returns a Guest on freshly cleared state.
     */
    public UserSession getCurrentSession() {
        if (!isLoggedIn()) {
            return null; // Signals we need login
        }

        boolean isGuest = prefs.getBoolean(KEY_IS_GUEST, true);
        if (isGuest) {
            return UserSession.createGuestSession();
        }

        long userId = prefs.getLong(KEY_USER_ID, -1);
        if (userId == -1) {
            // Unintended state, return a guest instead.
            return UserSession.createGuestSession();
        }

        String username = prefs.getString(KEY_USERNAME, "UnknownUser");
        return UserSession.createUserSession(userId, username);
    }

    /**
     * Persist strong user context when they login securely or register freshly.
     * Maps the offline UserAccount directly to preferences cleanly.
     */
    public void startRegisteredSession(UserAccount account) {
        prefs.edit()
            .putBoolean(KEY_HAS_SESSION, true)
            .putBoolean(KEY_IS_GUEST, false)
            .putLong(KEY_USER_ID, account.getUserId())
            .putString(KEY_USERNAME, account.getUsername())
            .apply();
    }

    /**
     * Switch context intentionally to a Guest session.
     */
    public void startGuestSession() {
        prefs.edit()
            .putBoolean(KEY_HAS_SESSION, true)
            .putBoolean(KEY_IS_GUEST, true)
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .apply();
    }

    /**
     * Checks if the user is currently verified either as a guest or a fully registered user.
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_HAS_SESSION, false);
    }

    /**
     * Identifies natively whether the active session belongs to a guest user.
     */
    public boolean isGuest() {
        return isLoggedIn() && prefs.getBoolean(KEY_IS_GUEST, true);
    }

    /**
     * Logout clears the active session but preserves saved account data.
     * Removes the active session locally triggering a return to the auth screen,
     * maintaining the underlying registered data naturally in SQLite.
     */
    public void logout() {
        clearSession();
    }

    /**
     * Flushes currently tracked context completely.
     */
    public void clearSession() {
        prefs.edit().clear().apply();
    }

    /**
     * Testing utility ONLY. Resets the singleton instance between test runs
     * to prevent corrupted mock contexts from bleeding across isolated tests.
     */
    static void clearInstanceForTest() {
        instance = null;
    }
}
