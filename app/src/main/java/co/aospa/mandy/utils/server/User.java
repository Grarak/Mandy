package co.aospa.mandy.utils.server;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.aospa.mandy.utils.Json;
import co.aospa.mandy.utils.Prefs;

/**
 * Created by willi on 15.04.17.
 */

public class User {

    public String mApiToken;
    public String mName;
    private String mPassword;

    public boolean mAdmin;
    public boolean mModerator;

    public boolean mVerified;

    public String[] mFirebaseKey;

    public User(String json) {
        try {
            JSONObject j = new JSONObject(json);
            init(j);
        } catch (JSONException ignored) {
        }
    }

    public User(JSONObject j) {
        init(j);
    }

    private void init(JSONObject j) {
        mApiToken = Json.getString(j, "apitoken");
        mName = Json.getString(j, "name");
        mPassword = Json.getString(j, "password");
        mAdmin = Json.getBool(j, "admin");
        mModerator = Json.getBool(j, "moderator");
        mVerified = Json.getBool(j, "verified");

        try {
            JSONArray keys = j.getJSONArray("firebasekey");
            mFirebaseKey = new String[keys.length()];
            for (int i = 0; i < mFirebaseKey.length; i++) {
                mFirebaseKey[i] = keys.getString(i);
            }
        } catch (JSONException ignored) {
        }
    }

    public User(String name, String password) {
        this(name, password, null);
    }

    public User(String name, String password, String firebaseKey) {
        mName = name;
        mPassword = password;
        mFirebaseKey = new String[1];
        mFirebaseKey[0] = firebaseKey;
    }

    @Override
    public String toString() {
        return toJSONObject().toString();
    }

    public JSONObject toJSONObject() {
        JSONObject j = new JSONObject();

        try {
            j.put("apitoken", mApiToken);
            j.put("name", mName);
            j.put("password", mPassword);
            j.put("admin", mAdmin);
            j.put("moderator", mModerator);
            j.put("verified", mVerified);

            if (mFirebaseKey != null) {
                JSONArray keys = new JSONArray();
                for (String key : mFirebaseKey) {
                    keys.put(key);
                }
                j.put("firebasekey", keys);
            }
        } catch (JSONException ignored) {
        }

        return j;
    }

    public boolean valid() {
        return mName != null;
    }

    public void cache(Context context) {
        Prefs.saveString("user", toString(), context);
    }

    public static User getCached(Context context) {
        String json = Prefs.getString("user", null, context);
        return json == null ? null : new User(json);
    }

    public static void deleteCache(Context context) {
        Prefs.remove("user", context);
    }

}
