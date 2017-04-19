package co.aospa.mandy.utils.server;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by willi on 15.04.17.
 */

public class Status {

    public final static int CODE_USERNAME_TAKEN = 3;
    public final static int CODE_USERNAME_PASSWORD_INVALID = 7;

    private int mCode;
    private boolean mValid;

    public Status(String json) {
        try {
            JSONObject j = new JSONObject(json);
            mCode = j.getInt("statuscode");

            mValid = true;
        } catch (JSONException ignored) {
            mValid = false;
        }
    }

    public int getCode() {
        return mCode;
    }

    public boolean valid() {
        return mValid;
    }

}
