package co.aospa.mandy.fragments;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import java.util.List;

import co.aospa.mandy.R;
import co.aospa.mandy.utils.Utils;
import co.aospa.mandy.utils.server.MandyApi;
import co.aospa.mandy.utils.server.NotiActivities;
import co.aospa.mandy.utils.server.User;
import co.aospa.mandy.view.recyclerview.ActivityView;
import co.aospa.mandy.view.recyclerview.Item;

/**
 * Created by willi on 19.04.17.
 */

public class ActivitiesFragment extends RecyclerViewFragment {

    private MandyApi mMandyApi;
    private SwipeRefreshLayout mRefreshLayout;

    @Override
    void init(View rootView) {
        super.init(rootView);

        mRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchActivities();
            }
        });
    }

    @Override
    void addItems(List<Item> items) {
        final NotiActivities notiActivities = NotiActivities.getCached(getActivity());
        if (notiActivities != null && notiActivities.valid()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setup(notiActivities);
                }
            });
        }
    }

    private void fetchActivities() {
        mMandyApi = new MandyApi("notification/activities", getActivity());
        mMandyApi.get(getUser().mApiToken, new MandyApi.ApiListener() {
            @Override
            public void onReturn(String output, int code) {
                NotiActivities notiActivities = new NotiActivities(output);
                if (notiActivities.valid()) {
                    notiActivities.cache(getActivity());
                    setup(notiActivities);

                    mRefreshLayout.setRefreshing(false);
                } else {
                    fail();
                }
            }

            @Override
            public void onFailure() {
                fail();
            }
        });
    }

    private void setup(NotiActivities notiActivities) {
        clearItems();

        User user = User.getCached(getActivity());
        for (NotiActivities.NotiActivity activity : notiActivities.mActivities) {
            ActivityView activityView = new ActivityView();
            activityView.setNotiActivity(user, activity);
            addItem(activityView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        fetchActivities();
    }

    @Override
    public void onPause() {
        super.onPause();

        stopFetching();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopFetching();
    }

    private void fail() {
        if (getActivity() != null) {
            Utils.toast(R.string.server_not_reachable, getActivity());
            mRefreshLayout.setRefreshing(false);
        }
    }

    private void stopFetching() {
        if (mMandyApi != null) {
            mMandyApi.disconnect();
        }
    }

    @Override
    int getRecyclerViewId() {
        return R.id.recyclerview;
    }

    @Override
    int getLayout() {
        return R.layout.fragment_recyclerview;
    }

}
