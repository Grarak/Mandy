package co.aospa.mandy.fragments;

/**
 * Created by willi on 18.04.17.
 */

public class VerfiedUsersFragment extends AllUsersFragment {

    @Override
    String getApiPath() {
        return "user/getverified";
    }

}
