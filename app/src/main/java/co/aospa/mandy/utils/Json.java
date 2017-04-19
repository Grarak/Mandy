package co.aospa.mandy.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by willi on 16.04.17.
 */

public class Json {

    public static String getString(JSONObject json, String key) {
        try {
            return json.getString(key);
        } catch (JSONException ignored) {
            return null;
        }
    }

    public static boolean getBool(JSONObject j, String key) {
        try {
            return j.getBoolean(key);
        } catch (JSONException ignored) {
            return false;
        }
    }

    public static int getInt(JSONObject j, String key) {
        try {
            return j.getInt(key);
        } catch (JSONException ignored) {
            return 0;
        }
    }

}
