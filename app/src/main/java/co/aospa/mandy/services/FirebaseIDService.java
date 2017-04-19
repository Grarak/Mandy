package co.aospa.mandy.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import co.aospa.mandy.utils.server.MandyApi;
import co.aospa.mandy.utils.server.User;

/**
 * Created by willi on 16.04.17.
 */

public class FirebaseIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        User user = User.getCached(this);
        if (user == null || user.mApiToken == null) return;

        sendToken(user);
    }

    public static void sendToken(User user) {
        user.mName = null;
        user.mFirebaseKey = new String[1];
        user.mFirebaseKey[0] = FirebaseInstanceId.getInstance().getId();

        MandyApi mandyApi = new MandyApi("account/firebasekey", null);
        mandyApi.post(user.toString(), user.mApiToken, null);
    }

}
