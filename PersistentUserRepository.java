package com.example.appdev_debugging.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.appdev_debugging.data.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class PersistentUserRepository implements UserRepository {
    private static final String PREFS_NAME = "user_accounts";
    private static final String KEY_PREFIX = "account_";

    private final SharedPreferences preferences;

    public PersistentUserRepository(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public synchronized User findByEmail(String email) {
        if (email == null) return null;
        String encoded = preferences.getString(keyFor(email), null);
        if (encoded == null) return null;

        try {
            JSONObject json = new JSONObject(encoded);
            return new User(
                    json.getString("fullName"),
                    json.getString("email"),
                    json.getString("passwordHash"),
                    json.getString("salt"));
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public synchronized boolean exists(String email) {
        return email != null && preferences.contains(keyFor(email));
    }

    @Override
    public synchronized boolean add(User user) {
        if (user == null || user.getEmail() == null) return false;

        String key = keyFor(user.getEmail());
        if (preferences.contains(key)) return false;

        try {
            JSONObject json = new JSONObject();
            json.put("fullName", user.getFullName());
            json.put("email", user.getEmail());
            json.put("passwordHash", user.getPasswordHash());
            json.put("salt", user.getSalt());
            preferences.edit().putString(key, json.toString()).apply();
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    private static String keyFor(String email) {
        return KEY_PREFIX + email.trim().toLowerCase(Locale.ROOT);
    }
}