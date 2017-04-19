package co.aospa.mandy.utils.server;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.aospa.mandy.utils.Json;
import co.aospa.mandy.utils.Prefs;

/**
 * Created by willi on 19.04.17.
 */

public class NotiActivities {

    public static class NotiActivity {

        public int mCode;
        public String mDate;
        public JSONObject mData;

        NotiActivity(JSONObject j) {
            mCode = Json.getInt(j, "code");
            mDate = Json.getString(j, "date");

            try {
                mData = j.getJSONObject("data");
            } catch (JSONException ignored) {
            }
        }

        JSONObject toJSONObject() {
            JSONObject j = new JSONObject();

            try {
                j.put("code", mCode);
                j.put("date", mDate);
                j.put("data", mData);
            } catch (JSONException ignored) {
            }

            return j;
        }

    }

    public NotiActivity[] mActivities;

    public NotiActivities(String json) {
        try {
            JSONArray array = new JSONArray(json);
            mActivities = new NotiActivity[array.length()];
            for (int i = 0; i < mActivities.length; i++) {
                mActivities[i] = new NotiActivity(array.getJSONObject(i));
            }
        } catch (JSONException ignored) {
        }
    }

    public boolean valid() {
        return mActivities != null;
    }

    @Override
    public String toString() {
        JSONArray array = new JSONArray();

        if (mActivities != null) {
            for (NotiActivity activity : mActivities) {
                array.put(activity.toJSONObject());
            }
        }

        return array.toString();
    }

    public void cache(Context context) {
        Prefs.saveString("notiactivities", toString(), context);
    }

    public static NotiActivities getCached(Context context) {
        String json = Prefs.getString("notiactivities", null, context);
        return json == null ? null : new NotiActivities(json);
    }

}
