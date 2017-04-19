package co.aospa.mandy.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import co.aospa.mandy.utils.server.User;

/**
 * Created by willi on 15.04.17.
 */

public class BaseFragment extends Fragment {

    private User mUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mUser = User.getCached(getActivity());
    }

    public void onUserUpdate(User user) {
        mUser = user;
    }

    public void onUserUpdateFailure() {
    }

    User getUser() {
        return mUser;
    }

}
