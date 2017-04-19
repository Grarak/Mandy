package co.aospa.mandy.utils.server;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import co.aospa.mandy.utils.Prefs;

/**
 * Created by willi on 18.04.17.
 */

public class Users {

    public User[] mUsers;

    private String mPrefName;

    public Users(String json) {
        this(json, "users");
    }

    public Users(String json, String prefName) {
        try {
            JSONArray j = new JSONArray(json);

            mUsers = new User[j.length()];
            for (int i = 0; i < mUsers.length; i++) {
                mUsers[i] = new User(j.getJSONObject(i));
            }
        } catch (JSONException ignored) {
        }

        mPrefName = prefName;
    }

    @Override
    public String toString() {
        JSONArray j = new JSONArray();

        for (User user : mUsers) {
            j.put(user.toJSONObject());
        }

        return j.toString();
    }

    public void cache(Context context) {
        Prefs.saveString(mPrefName, toString(), context);
    }

    public static Users getCached(Context context) {
        return getCached("users", context);
    }

    public boolean valid() {
        return mUsers != null && mUsers.length > 0;
    }

    public static Users getCached(String prefName, Context context) {
        String json = Prefs.getString(prefName, null, context);
        return json == null ? null : new Users(json, prefName);
    }

}
