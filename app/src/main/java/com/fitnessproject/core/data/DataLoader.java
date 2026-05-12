package com.fitnessproject.core.data;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataLoader {

    public static String loadJSONFromAsset(Context context, String fileName) {
        String json;
        try {
            InputStream is = context.getAssets().open("data/" + fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static List<String> getExerciseNames(Context context) {
        List<String> names = new ArrayList<>();
        try {
            String json = loadJSONFromAsset(context, "exercises.json");
            if (json != null) {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    names.add(array.getJSONObject(i).getString("name"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return names;
    }

    public static Map<String, List<String>> getExerciseNameToCategoriesMap(Context context) {
        Map<String, List<String>> nameToCategories = new LinkedHashMap<>();
        try {
            String json = loadJSONFromAsset(context, "exercises.json");
            if (json != null) {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String name = obj.getString("name");
                    if (name != null && !name.trim().isEmpty()) {
                        List<String> categories = extractCategoriesFromJson(obj);
                        nameToCategories.put(name.trim(), categories);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return nameToCategories;
    }

    public static List<String> getDefaultCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("Chest");
        categories.add("Back");
        categories.add("Legs");
        categories.add("Shoulders");
        categories.add("Biceps");
        categories.add("Triceps");
        categories.add("Arms");
        categories.add("Core");
        categories.add("Cardio");
        categories.add("Other");
        return categories;
    }

    public static List<String> getExerciseCategories(Context context, String exerciseName) {
        Map<String, List<String>> nameToCategories = getExerciseNameToCategoriesMap(context);
        if (exerciseName == null) return defaultOtherCategory();
        String trimmed = exerciseName.trim();
        if (trimmed.isEmpty()) return defaultOtherCategory();

        for (Map.Entry<String, List<String>> entry : nameToCategories.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(trimmed)) {
                if (entry.getValue() == null || entry.getValue().isEmpty()) {
                    return defaultOtherCategory();
                }
                return new ArrayList<>(entry.getValue());
            }
        }
        return defaultOtherCategory();
    }

    public static String getExerciseCategory(Context context, String exerciseName) {
        List<String> categories = getExerciseCategories(context, exerciseName);
        if (categories.isEmpty()) {
            return "Other";
        }
        return categories.get(0);
    }

    public static String mapNameToId(Context context, String name) {
        try {
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "exercises.json"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.getString("name").equals(name)) {
                    return obj.getString("id");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "bench"; // default
    }

    public static JSONObject getQuestionsForExercise(Context context, String exerciseId) {
        try {
            JSONObject allQuestions = new JSONObject(loadJSONFromAsset(context, "questions.json"));
            if (allQuestions.has(exerciseId)) {
                JSONArray exerciseQuestions = allQuestions.getJSONArray(exerciseId);
                if (exerciseQuestions.length() > 0) {
                    return exerciseQuestions.getJSONObject(0); // For MVP, return the first question
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getRules(Context context) {
        try {
            String json = loadJSONFromAsset(context, "rules.json");
            if (json == null) return new JSONObject();
            return new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public static JSONObject getNextSteps(Context context) {
        try {
            String json = loadJSONFromAsset(context, "next_steps.json");
            if (json == null) return new JSONObject();
            return new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public static List<String> parseCategoryCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return defaultOtherCategory();
        }
        String[] parts = csv.split(",");
        Set<String> unique = new LinkedHashSet<>();
        for (String part : parts) {
            String normalized = normalizeCategory(part);
            if (!normalized.isEmpty()) {
                unique.add(normalized);
            }
        }
        if (unique.isEmpty()) {
            unique.add("Other");
        }
        return new ArrayList<>(unique);
    }

    public static String toCategoryCsv(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return "Other";
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String category : categories) {
            String normalized = normalizeCategory(category);
            if (!normalized.isEmpty()) {
                unique.add(normalized);
            }
        }
        if (unique.isEmpty()) {
            return "Other";
        }
        return String.join(", ", unique);
    }

    private static String normalizeCategory(String raw) {
        if (raw == null) return "Other";
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return "Other";

        for (String category : getDefaultCategories()) {
            if (category.equalsIgnoreCase(trimmed)) {
                return category;
            }
        }
        return "Other";
    }

    private static List<String> extractCategoriesFromJson(JSONObject obj) throws JSONException {
        Set<String> categories = new LinkedHashSet<>();
        if (obj.has("groups")) {
            JSONArray groups = obj.getJSONArray("groups");
            for (int i = 0; i < groups.length(); i++) {
                categories.add(normalizeCategory(groups.getString(i)));
            }
        } else if (obj.has("group")) {
            categories.add(normalizeCategory(obj.optString("group", "Other")));
        }
        if (categories.isEmpty()) {
            categories.add("Other");
        }
        return new ArrayList<>(categories);
    }

    private static List<String> defaultOtherCategory() {
        List<String> list = new ArrayList<>();
        list.add("Other");
        return list;
    }
}
