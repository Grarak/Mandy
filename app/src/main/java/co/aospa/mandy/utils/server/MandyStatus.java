package co.aospa.mandy.utils.server;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.aospa.mandy.utils.Json;
import co.aospa.mandy.utils.Prefs;

/**
 * Created by willi on 16.04.17.
 */

public class MandyStatus {

    public static class AospaProject {

        public String mName;
        public String mLatestTag;
        public boolean mConflicted;

        public AospaProject(JSONObject json) {
            mName = Json.getString(json, "name");
            mLatestTag = Json.getString(json, "latesttag");
            mConflicted = Json.getBool(json, "conflicted");
        }

        public JSONObject toJson() {
            try {
                JSONObject j = new JSONObject();
                j.put("name", mName);
                j.put("latesttag", mLatestTag);
                j.put("conflicted", mConflicted);

                return j;
            } catch (JSONException ignored) {
            }

            return null;
        }

        @Override
        public String toString() {
            try {
                JSONObject j = new JSONObject();
                j.put("name", mName);
                j.put("latesttag", mLatestTag);
                j.put("conflicted", mConflicted);

                return j.toString();
            } catch (JSONException ignored) {
            }
            return null;
        }
    }

    public String mManifestTag;
    public String mLatestTag;

    public boolean mMergeable;
    public boolean mMerging;
    public boolean mMerged;
    public User mMerger;

    public boolean mSubmittable;
    public boolean mSubmitting;
    public boolean mSubmitted;
    public User mSubmitter;

    public boolean mReverting;
    public User mReverter;

    public AospaProject[] mAospaProjects;

    public MandyStatus(String json) {
        try {
            JSONObject j = new JSONObject(json);

            mManifestTag = Json.getString(j, "manifesttag");
            mLatestTag = Json.getString(j, "latesttag");

            mMergeable = Json.getBool(j, "mergeable");
            mMerging = Json.getBool(j, "merging");
            mMerged = Json.getBool(j, "merged");
            mMerger = new User(j.getJSONObject("merger"));

            mSubmittable = Json.getBool(j, "submittable");
            mSubmitting = Json.getBool(j, "submitting");
            mSubmitted = Json.getBool(j, "submitted");
            mSubmitter = new User(j.getJSONObject("submitter"));

            mReverting = Json.getBool(j, "reverting");
            mReverter = new User(j.getJSONObject("reverter"));

            if (j.has("projects")) {
                JSONArray projectArray = j.getJSONArray("projects");
                mAospaProjects = new AospaProject[projectArray.length()];
                for (int i = 0; i < mAospaProjects.length; i++) {
                    mAospaProjects[i] = new AospaProject(projectArray.getJSONObject(i));
                }
            }
        } catch (JSONException ignored) {
        }
    }

    public boolean valid() {
        return mManifestTag != null;
    }

    @Override
    public String toString() {
        try {
            JSONObject j = new JSONObject();

            j.put("manifesttag", mManifestTag);
            j.put("latesttag", mLatestTag);
            j.put("mergeable", mMergeable);
            j.put("merging", mMerging);
            j.put("merged", mMerged);
            j.put("merger", mMerger.toJSONObject());
            j.put("submitting", mSubmitting);
            j.put("submitted", mSubmitted);
            j.put("submitter", mSubmitter.toJSONObject());
            j.put("reverting", mReverting);
            j.put("reverter", mReverter.toJSONObject());

            if (mAospaProjects != null) {
                JSONArray array = new JSONArray();
                for (AospaProject aospaProject : mAospaProjects) {
                    array.put(aospaProject.toJson());
                }
                j.put("projects", array);
            }
            return j.toString();
        } catch (JSONException ignored) {
        }

        return null;
    }

    public static MandyStatus getCached(Context context) {
        String json = Prefs.getString("mandystatus", null, context);
        return json == null ? null : new MandyStatus(json);
    }

    public void cache(Context context) {
        Prefs.saveString("mandystatus", toString(), context);
    }

}
