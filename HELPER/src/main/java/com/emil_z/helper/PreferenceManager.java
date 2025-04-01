package com.emil_z.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    public static void writeToSharedPreferences(Context context, String fileName, Object[][] values) {
        SharedPreferences.Editor editor = context.getSharedPreferences(fileName, Context.MODE_PRIVATE).edit();

        for (Object[] entry : values) {
            if (entry.length != 3) {
                throw new IllegalArgumentException("Each entry must have 3 elements: name, value, and type");
            }

            String name = (String) entry[0];
            Object value = entry[1];
            String type = (String) entry[2];

            switch (type.toLowerCase()) {
                case "string":
                    editor.putString(name, (String) value);
                    break;
                case "int":
                    editor.putInt(name, (Integer) value);
                    break;
                case "boolean":
                    editor.putBoolean(name, (Boolean) value);
                    break;
                case "float":
                    editor.putFloat(name, (Float) value);
                    break;
                case "long":
                    editor.putLong(name, (Long) value);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported type: " + type);
            }
        }

        editor.apply();
    }

    public static Object[][] readFromSharedPreferences(Context context, String fileName, Object[][] keys) {
        SharedPreferences prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        Object[][] results = new Object[keys.length][3];

        for (int i = 0; i < keys.length; i++) {
            if (keys[i].length != 2) {
                //throw new IllegalArgumentException("Each key entry must have 2 elements: name and type");
                results = null;
            }

            String name = (String) keys[i][0];
            String type = (String) keys[i][1];
            Object value = null;

            switch (type.toLowerCase()) {
                case "string":
                    value = prefs.getString(name, null);
                    break;
                case "int":
                    value = prefs.getInt(name, 0);
                    break;
                case "boolean":
                    value = prefs.getBoolean(name, false);
                    break;
                case "float":
                    value = prefs.getFloat(name, 0f);
                    break;
                case "long":
                    value = prefs.getLong(name, 0L);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported type: " + type);
            }

            results[i] = new Object[]{name, value, type};
        }

        return results;
    }
}