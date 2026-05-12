package com.fitnessproject.core.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fitnessproject.core.data.model.UserSession;
import com.fitnessproject.core.session.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String GROUP_HEADER_PREFIX = "__HEADER__:";

    private static final String DATABASE_NAME = "fitness_project.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_WORKOUTS = "workouts";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EXERCISE = "exercise";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_REPS = "reps";
    public static final String COLUMN_SETS = "sets";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_EXERCISE_GROUP = "exercise_group";

    private final Context appContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_WORKOUTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EXERCISE + " TEXT, " +
                COLUMN_WEIGHT + " TEXT, " +
                COLUMN_REPS + " TEXT, " +
                COLUMN_SETS + " TEXT, " +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_EXERCISE_GROUP + " TEXT, " +
                COLUMN_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_WORKOUTS + " ADD COLUMN " + COLUMN_USER_ID + " INTEGER");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_WORKOUTS + " ADD COLUMN " + COLUMN_EXERCISE_GROUP + " TEXT");
        }
    }

    public void addWorkout(String exercise, String weight, String reps, String sets) {
        addWorkout(exercise, weight, reps, sets, null);
    }

    public void addWorkout(String exercise, String weight, String reps, String sets, String exerciseGroup) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXERCISE, exercise);
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_REPS, reps);
        values.put(COLUMN_SETS, sets);
        values.put(COLUMN_EXERCISE_GROUP, exerciseGroup);

        Long currentUserId = getCurrentUserIdOrNull();
        if (currentUserId == null) {
            values.putNull(COLUMN_USER_ID);
        } else {
            values.put(COLUMN_USER_ID, currentUserId);
        }

        db.insert(TABLE_WORKOUTS, null, values);
        db.close();
    }

    public List<String> getAllWorkouts() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        Long currentUserId = getCurrentUserIdOrNull();
        if (currentUserId == null) {
            cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_WORKOUTS + " WHERE " + COLUMN_USER_ID + " IS NULL ORDER BY id DESC",
                    null
            );
        } else {
            cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_WORKOUTS + " WHERE " + COLUMN_USER_ID + " = ? ORDER BY id DESC",
                    new String[]{String.valueOf(currentUserId)}
            );
        }

        if (cursor.moveToFirst()) {
            do {
                String entry = cursor.getString(1) + " - " +
                        cursor.getString(2) + " lbs x " +
                        cursor.getString(3) + " reps (" +
                        cursor.getString(4) + " sets)";
                list.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<String> getProgressForExercise(String exercise) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        Long currentUserId = getCurrentUserIdOrNull();
        if (currentUserId == null) {
            cursor = db.query(
                    TABLE_WORKOUTS,
                    null,
                    COLUMN_EXERCISE + "=? AND " + COLUMN_USER_ID + " IS NULL",
                    new String[]{exercise},
                    null,
                    null,
                    "id DESC"
            );
        } else {
            cursor = db.query(
                    TABLE_WORKOUTS,
                    null,
                    COLUMN_EXERCISE + "=? AND " + COLUMN_USER_ID + "=?",
                    new String[]{exercise, String.valueOf(currentUserId)},
                    null,
                    null,
                    "id DESC"
            );
        }

        if (cursor.moveToFirst()) {
            do {
                String entry = cursor.getString(2) + " lbs x " +
                        cursor.getString(3) + " reps (" +
                        cursor.getString(4) + " sets)";
                list.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<String> getAllWorkoutsGroupedByDate() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        Long currentUserId = getCurrentUserIdOrNull();
        if (currentUserId == null) {
            cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_WORKOUTS + " WHERE " + COLUMN_USER_ID + " IS NULL ORDER BY " +
                            COLUMN_DATE + " DESC, " + COLUMN_ID + " DESC",
                    null
            );
        } else {
            cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_WORKOUTS + " WHERE " + COLUMN_USER_ID + " = ? ORDER BY " +
                            COLUMN_DATE + " DESC, " + COLUMN_ID + " DESC",
                    new String[]{String.valueOf(currentUserId)}
            );
        }

        String currentDateHeader = null;
        if (cursor.moveToFirst()) {
            do {
                String dateText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String dateHeader = formatDateHeader(dateText);
                if (!dateHeader.equals(currentDateHeader)) {
                    list.add(GROUP_HEADER_PREFIX + dateHeader);
                    currentDateHeader = dateHeader;
                }

                String entry = formatTimeText(dateText) + "  "
                        + cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXERCISE)) + " - "
                        + cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT)) + " lbs x "
                        + cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPS)) + " reps ("
                        + cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETS)) + " sets)";
                list.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<String> getProgressForExerciseGroupedByDate(String exercise) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        Long currentUserId = getCurrentUserIdOrNull();
        if (currentUserId == null) {
            cursor = db.query(
                    TABLE_WORKOUTS,
                    null,
                    COLUMN_EXERCISE + "=? AND " + COLUMN_USER_ID + " IS NULL",
                    new String[]{exercise},
                    null,
                    null,
                    COLUMN_DATE + " DESC, " + COLUMN_ID + " DESC"
            );
        } else {
            cursor = db.query(
                    TABLE_WORKOUTS,
                    null,
                    COLUMN_EXERCISE + "=? AND " + COLUMN_USER_ID + "=?",
                    new String[]{exercise, String.valueOf(currentUserId)},
                    null,
                    null,
                    COLUMN_DATE + " DESC, " + COLUMN_ID + " DESC"
            );
        }

        String currentDateHeader = null;
        if (cursor.moveToFirst()) {
            do {
                String dateText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String dateHeader = formatDateHeader(dateText);
                if (!dateHeader.equals(currentDateHeader)) {
                    list.add(GROUP_HEADER_PREFIX + dateHeader);
                    currentDateHeader = dateHeader;
                }

                String entry = formatTimeText(dateText) + "  "
                        + cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT)) + " lbs x "
                        + cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPS)) + " reps ("
                        + cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETS)) + " sets)";
                list.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public String getLatestSetSummaryForExercise(String exercise) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        Long currentUserId = getCurrentUserIdOrNull();
        if (currentUserId == null) {
            cursor = db.query(
                    TABLE_WORKOUTS,
                    null,
                    COLUMN_EXERCISE + "=? AND " + COLUMN_USER_ID + " IS NULL",
                    new String[]{exercise},
                    null,
                    null,
                    COLUMN_DATE + " DESC, " + COLUMN_ID + " DESC",
                    "1"
            );
        } else {
            cursor = db.query(
                    TABLE_WORKOUTS,
                    null,
                    COLUMN_EXERCISE + "=? AND " + COLUMN_USER_ID + "=?",
                    new String[]{exercise, String.valueOf(currentUserId)},
                    null,
                    null,
                    COLUMN_DATE + " DESC, " + COLUMN_ID + " DESC",
                    "1"
            );
        }

        String latest = null;
        if (cursor.moveToFirst()) {
            String dateText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
            latest = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT)) + " lbs x "
                    + cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPS)) + " reps ("
                    + cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETS)) + " sets)"
                    + " • " + formatDateTimeText(dateText);
        }

        cursor.close();
        db.close();
        return latest;
    }

    public void deleteAllWorkouts() {
        SQLiteDatabase db = this.getWritableDatabase();
        Long currentUserId = getCurrentUserIdOrNull();
        if (currentUserId == null) {
            db.delete(TABLE_WORKOUTS, COLUMN_USER_ID + " IS NULL", null);
        } else {
            db.delete(TABLE_WORKOUTS, COLUMN_USER_ID + "=?", new String[]{String.valueOf(currentUserId)});
        }
        db.close();
    }

    public int getWorkoutCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        Long currentUserId = getCurrentUserIdOrNull();
        if (currentUserId == null) {
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_WORKOUTS + " WHERE " + COLUMN_USER_ID + " IS NULL",
                    null
            );
        } else {
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_WORKOUTS + " WHERE " + COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(currentUserId)}
            );
        }

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public List<LastSessionEntry> getLastSessionEntries() {
        List<LastSessionEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        Long currentUserId = getCurrentUserIdOrNull();
        String dateSubquery;
        if (currentUserId == null) {
            dateSubquery = "(SELECT DATE(MAX(" + COLUMN_DATE + ")) FROM " + TABLE_WORKOUTS
                    + " WHERE " + COLUMN_USER_ID + " IS NULL)";
            cursor = db.rawQuery(
                    "SELECT " + COLUMN_EXERCISE + ", " + COLUMN_WEIGHT + ", "
                            + COLUMN_REPS + ", " + COLUMN_SETS
                            + " FROM " + TABLE_WORKOUTS
                            + " WHERE " + COLUMN_USER_ID + " IS NULL"
                            + " AND DATE(" + COLUMN_DATE + ") = " + dateSubquery
                            + " ORDER BY " + COLUMN_ID + " DESC",
                    null
            );
        } else {
            dateSubquery = "(SELECT DATE(MAX(" + COLUMN_DATE + ")) FROM " + TABLE_WORKOUTS
                    + " WHERE " + COLUMN_USER_ID + " = ?)";
            cursor = db.rawQuery(
                    "SELECT " + COLUMN_EXERCISE + ", " + COLUMN_WEIGHT + ", "
                            + COLUMN_REPS + ", " + COLUMN_SETS
                            + " FROM " + TABLE_WORKOUTS
                            + " WHERE " + COLUMN_USER_ID + " = ?"
                            + " AND DATE(" + COLUMN_DATE + ") = " + dateSubquery
                            + " ORDER BY " + COLUMN_ID + " DESC",
                    new String[]{String.valueOf(currentUserId), String.valueOf(currentUserId)}
            );
        }

        if (cursor.moveToFirst()) {
            do {
                String exercise = cursor.getString(0);
                if (exercise != null && !exercise.trim().isEmpty()) {
                    entries.add(new LastSessionEntry(
                            exercise.trim(),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3)
                    ));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return entries;
    }

    public List<String> getTrackedExerciseNames() {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        Long currentUserId = getCurrentUserIdOrNull();
        if (currentUserId == null) {
            cursor = db.rawQuery(
                    "SELECT DISTINCT " + COLUMN_EXERCISE + " FROM " + TABLE_WORKOUTS +
                            " WHERE " + COLUMN_USER_ID + " IS NULL ORDER BY " + COLUMN_EXERCISE + " COLLATE NOCASE ASC",
                    null
            );
        } else {
            cursor = db.rawQuery(
                    "SELECT DISTINCT " + COLUMN_EXERCISE + " FROM " + TABLE_WORKOUTS +
                            " WHERE " + COLUMN_USER_ID + " = ? ORDER BY " + COLUMN_EXERCISE + " COLLATE NOCASE ASC",
                    new String[]{String.valueOf(currentUserId)}
            );
        }

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                if (name != null && !name.trim().isEmpty()) {
                    names.add(name);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return names;
    }

    public List<ExerciseGroupEntry> getTrackedExercisesWithGroups() {
        List<ExerciseGroupEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        Long currentUserId = getCurrentUserIdOrNull();
        if (currentUserId == null) {
            cursor = db.rawQuery(
                    "SELECT " + COLUMN_EXERCISE + ", COALESCE(GROUP_CONCAT(DISTINCT " + COLUMN_EXERCISE_GROUP + "), '') " +
                            "FROM " + TABLE_WORKOUTS +
                            " WHERE " + COLUMN_USER_ID + " IS NULL GROUP BY " + COLUMN_EXERCISE,
                    null
            );
        } else {
            cursor = db.rawQuery(
                    "SELECT " + COLUMN_EXERCISE + ", COALESCE(GROUP_CONCAT(DISTINCT " + COLUMN_EXERCISE_GROUP + "), '') " +
                            "FROM " + TABLE_WORKOUTS +
                            " WHERE " + COLUMN_USER_ID + " = ? GROUP BY " + COLUMN_EXERCISE,
                    new String[]{String.valueOf(currentUserId)}
            );
        }

        if (cursor.moveToFirst()) {
            do {
                String exercise = cursor.getString(0);
                String group = cursor.getString(1);
                if (exercise != null && !exercise.trim().isEmpty()) {
                    entries.add(new ExerciseGroupEntry(exercise.trim(), group == null ? "" : group.trim()));
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return entries;
    }

    public UserStatsSummary getUserStatsSummary() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        Long currentUserId = getCurrentUserIdOrNull();
        if (currentUserId == null) {
            cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_WORKOUTS + " WHERE " + COLUMN_USER_ID + " IS NULL ORDER BY " +
                            COLUMN_DATE + " DESC, " + COLUMN_ID + " DESC",
                    null
            );
        } else {
            cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_WORKOUTS + " WHERE " + COLUMN_USER_ID + " = ? ORDER BY " +
                            COLUMN_DATE + " DESC, " + COLUMN_ID + " DESC",
                    new String[]{String.valueOf(currentUserId)}
            );
        }

        UserStatsSummary summary = new UserStatsSummary();
        Map<String, Integer> exerciseCounts = new LinkedHashMap<>();
        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        Set<String> activeDateKeys = new LinkedHashSet<>();

        // Optimization: load the category map once
        Map<String, List<String>> categoryMap = DataLoader.getExerciseNameToCategoriesMap(appContext);

        Calendar now = Calendar.getInstance();
        Date newestWorkoutDate = null;
        Date oldestWorkoutDate = null;
        double heaviestWeight = Double.NEGATIVE_INFINITY;
        String heaviestExercise = "";
        int heaviestReps = 0;
        int heaviestSets = 0;

        if (cursor.moveToFirst()) {
            do {
                summary.totalWorkouts++;

                String exercise = valueOrEmpty(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXERCISE)));
                String weightRaw = valueOrEmpty(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT)));
                String repsRaw = valueOrEmpty(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPS)));
                String setsRaw = valueOrEmpty(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETS)));
                String exerciseGroup = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXERCISE_GROUP));
                String dateText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));

                Date parsedDate = parseDatabaseDate(dateText);
                if (parsedDate != null) {
                    if (newestWorkoutDate == null || parsedDate.after(newestWorkoutDate)) {
                        newestWorkoutDate = parsedDate;
                    }
                    if (oldestWorkoutDate == null || parsedDate.before(oldestWorkoutDate)) {
                        oldestWorkoutDate = parsedDate;
                    }

                    if (isWithinLastDays(parsedDate, now, 7)) {
                        summary.workoutsLast7Days++;
                    }
                    if (isWithinLastDays(parsedDate, now, 30)) {
                        summary.workoutsLast30Days++;
                    }
                    if (isSameMonth(parsedDate, now)) {
                        activeDateKeys.add(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(parsedDate));
                    }
                }

                if (!exercise.isEmpty()) {
                    incrementCount(exerciseCounts, exercise);
                }

                int reps = parseIntegerSafely(repsRaw);
                int sets = parseIntegerSafely(setsRaw);
                summary.totalSets += Math.max(sets, 0);
                if (reps > 0 && sets > 0) {
                    summary.totalReps += reps * sets;
                }

                double weight = parseDoubleSafely(weightRaw);
                if (weight > heaviestWeight) {
                    heaviestWeight = weight;
                    heaviestExercise = exercise;
                    heaviestReps = reps;
                    heaviestSets = sets;
                }

                if (weight > 0 && reps > 0 && sets > 0) {
                    summary.totalVolume += (weight * reps * sets);
                }

                List<String> categories = DataLoader.parseCategoryCsv(exerciseGroup);
                if ((exerciseGroup == null || exerciseGroup.trim().isEmpty()) && !exercise.isEmpty()) {
                    // Use cached map for performance
                    String trimmed = exercise.trim();
                    boolean found = false;
                    for (Map.Entry<String, List<String>> entry : categoryMap.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(trimmed)) {
                            categories = new ArrayList<>(entry.getValue());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        categories = new ArrayList<>();
                        categories.add("Other");
                    }
                }
                for (String category : categories) {
                    incrementCount(categoryCounts, category);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        summary.distinctExercises = exerciseCounts.size();
        summary.activeDaysThisMonth = activeDateKeys.size();
        summary.lastWorkoutDateLabel = newestWorkoutDate == null
                ? "No workouts yet"
                : formatDateTimeText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(newestWorkoutDate));
        summary.lastWorkoutDayLabel = newestWorkoutDate == null ? "No recent session" : formatDateHeader(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(newestWorkoutDate)
        );
        summary.averageWorkoutsPerWeek = calculateAveragePerWeek(summary.totalWorkouts, oldestWorkoutDate, newestWorkoutDate);
        summary.mostLoggedExerciseLabel = formatTopLabel(exerciseCounts, "No exercise data");
        summary.favoriteCategoryLabel = formatTopLabel(categoryCounts, "No category data");
        summary.heaviestSetLabel = formatHeaviestSetLabel(heaviestWeight, heaviestExercise, heaviestReps, heaviestSets);
        summary.totalVolumeLabel = formatVolumeLabel(summary.totalVolume);
        summary.hasData = summary.totalWorkouts > 0;
        return summary;
    }

    private String formatVolumeLabel(double totalVolume) {
        if (totalVolume <= 0) return "0 lbs";
        if (totalVolume >= 1000000) {
            return String.format(Locale.US, "%.2fM lbs", totalVolume / 1000000.0);
        } else if (totalVolume >= 1000) {
            return String.format(Locale.US, "%.1fK lbs", totalVolume / 1000.0);
        }
        return formatWeightText(totalVolume) + " lbs";
    }

    private Long getCurrentUserIdOrNull() {
        UserSession session = SessionManager.getInstance(appContext).getCurrentSession();
        if (session != null && !session.isGuest()) {
            return session.getUserId();
        }
        return null;
    }

    private void incrementCount(Map<String, Integer> counts, String key) {
        if (key == null || key.trim().isEmpty()) {
            return;
        }
        Integer current = counts.get(key);
        counts.put(key, current == null ? 1 : current + 1);
    }

    private int parseIntegerSafely(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private double parseDoubleSafely(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Double.NEGATIVE_INFINITY;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ignored) {
            return Double.NEGATIVE_INFINITY;
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isWithinLastDays(Date date, Calendar now, int days) {
        Calendar threshold = (Calendar) now.clone();
        threshold.add(Calendar.DAY_OF_YEAR, -(days - 1));
        resetToStartOfDay(threshold);

        Calendar target = Calendar.getInstance();
        target.setTime(date);
        return !target.before(threshold) && !target.after(now);
    }

    private boolean isSameMonth(Date date, Calendar reference) {
        Calendar target = Calendar.getInstance();
        target.setTime(date);
        return target.get(Calendar.YEAR) == reference.get(Calendar.YEAR)
                && target.get(Calendar.MONTH) == reference.get(Calendar.MONTH);
    }

    private void resetToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private double calculateAveragePerWeek(int totalWorkouts, Date oldestWorkoutDate, Date newestWorkoutDate) {
        if (totalWorkouts <= 0 || oldestWorkoutDate == null || newestWorkoutDate == null) {
            return 0d;
        }
        long diffMillis = newestWorkoutDate.getTime() - oldestWorkoutDate.getTime();
        double totalDays = Math.max(1d, (diffMillis / 86400000d) + 1d);
        return (totalWorkouts / totalDays) * 7d;
    }

    private String formatTopLabel(Map<String, Integer> counts, String emptyLabel) {
        String bestKey = null;
        int bestCount = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                bestKey = entry.getKey();
            }
        }
        if (bestKey == null) {
            return emptyLabel;
        }
        return bestKey + " • " + bestCount + (bestCount == 1 ? " session" : " sessions");
    }

    private String formatHeaviestSetLabel(double heaviestWeight, String exercise, int reps, int sets) {
        if (heaviestExerciseMissing(heaviestWeight, exercise)) {
            return "No weighted sets yet";
        }

        StringBuilder label = new StringBuilder();
        label.append(formatWeightText(heaviestWeight)).append(" lbs");
        if (!exercise.trim().isEmpty()) {
            label.append(" • ").append(exercise.trim());
        }
        if (reps > 0) {
            label.append(" x ").append(reps);
        }
        if (sets > 0) {
            label.append(" (").append(sets).append(sets == 1 ? " set)" : " sets)");
        }
        return label.toString();
    }

    private boolean heaviestExerciseMissing(double heaviestWeight, String exercise) {
        return heaviestWeight == Double.NEGATIVE_INFINITY || exercise == null || exercise.trim().isEmpty();
    }

    private String formatWeightText(double weight) {
        if (weight == Math.rint(weight)) {
            return String.valueOf((int) weight);
        }
        return String.format(Locale.US, "%.1f", weight);
    }

    private String formatDateHeader(String rawDate) {
        Date parsed = parseDatabaseDate(rawDate);
        if (parsed == null) {
            return "Unknown Date";
        }

        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        Calendar target = Calendar.getInstance();
        target.setTime(parsed);

        if (isSameDay(target, today)) {
            return "Today";
        }
        if (isSameDay(target, yesterday)) {
            return "Yesterday";
        }
        return new SimpleDateFormat("EEE, MMM d, yyyy", Locale.US).format(parsed);
    }

    private String formatTimeText(String rawDate) {
        Date parsed = parseDatabaseDate(rawDate);
        if (parsed == null) {
            return "--:--";
        }
        return new SimpleDateFormat("h:mm a", Locale.US).format(parsed);
    }

    private String formatDateTimeText(String rawDate) {
        Date parsed = parseDatabaseDate(rawDate);
        if (parsed == null) {
            return "Unknown time";
        }
        return new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US).format(parsed);
    }

    private Date parseDatabaseDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            return parser.parse(rawDate);
        } catch (ParseException ignored) {
            return null;
        }
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static class LastSessionEntry {
        public final String exercise;
        public final String weight;
        public final String reps;
        public final String sets;

        public LastSessionEntry(String exercise, String weight, String reps, String sets) {
            this.exercise = exercise;
            this.weight = weight;
            this.reps = reps;
            this.sets = sets;
        }
    }

    public static class ExerciseGroupEntry {
        private final String exerciseName;
        private final String exerciseGroup;

        public ExerciseGroupEntry(String exerciseName, String exerciseGroup) {
            this.exerciseName = exerciseName;
            this.exerciseGroup = exerciseGroup;
        }

        public String getExerciseName() {
            return exerciseName;
        }

        public String getExerciseGroup() {
            return exerciseGroup;
        }
    }

    public static class UserStatsSummary {
        public boolean hasData;
        public int totalWorkouts;
        public int totalSets;
        public int totalReps;
        public double totalVolume;
        public int distinctExercises;
        public int workoutsLast7Days;
        public int workoutsLast30Days;
        public int activeDaysThisMonth;
        public double averageWorkoutsPerWeek;
        public String lastWorkoutDayLabel = "No recent session";
        public String lastWorkoutDateLabel = "No workouts yet";
        public String mostLoggedExerciseLabel = "No exercise data";
        public String heaviestSetLabel = "No weighted sets yet";
        public String favoriteCategoryLabel = "No category data";
        public String totalVolumeLabel = "0 lbs";
    }
}
