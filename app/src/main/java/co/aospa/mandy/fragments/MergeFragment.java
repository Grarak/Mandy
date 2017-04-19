package co.aospa.mandy.fragments;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import co.aospa.mandy.NavigationActivity;
import co.aospa.mandy.R;
import co.aospa.mandy.utils.Utils;
import co.aospa.mandy.utils.server.MandyApi;
import co.aospa.mandy.utils.server.MandyStatus;
import co.aospa.mandy.utils.server.User;
import co.aospa.mandy.view.MandyStatusView;
import co.aospa.mandy.view.recyclerview.Item;
import co.aospa.mandy.view.recyclerview.RepoView;

/**
 * Created by willi on 16.04.17.
 */

public class MergeFragment extends RecyclerViewFragment implements
        MandyStatusView.MandyStatusViewListener, RepoView.RepoListener {

    private MandyStatusView mMandyStatusView;
    private SwipeRefreshLayout mRefreshLayout;

    private MandyApi mMandyApi;

    @Override
    void init(View rootView) {
        super.init(rootView);

        mMandyStatusView = (MandyStatusView) rootView.findViewById(R.id.mandy_status);

        mRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchStatus();
            }
        });
    }

    @Override
    void addItems(List<Item> items) {
        final MandyStatus status = MandyStatus.getCached(getActivity());
        if (status != null && status.valid()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setup(status);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        fetchStatus();
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

    private void fetchStatus() {
        mRefreshLayout.setRefreshing(true);
        ((NavigationActivity) getActivity()).updateUser();
    }

    @Override
    public void onUserUpdate(User user) {
        mMandyApi = new MandyApi("status/get", getActivity());
        mMandyApi.get(user.mApiToken, new MandyApi.ApiListener() {
            @Override
            public void onReturn(String output, int code) {
                MandyStatus mandyStatus = new MandyStatus(output);
                if (mandyStatus.valid()) {
                    mandyStatus.cache(getActivity());
                    setup(mandyStatus);

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

    @Override
    public void onUserUpdateFailure() {
        fail();
    }

    private void setup(MandyStatus status) {
        clearItems();

        mMandyStatusView.setStatus(status, this);
        mMandyStatusView.setVisibility(View.VISIBLE);

        if (Utils.getOrientation(getActivity()) != Configuration.ORIENTATION_LANDSCAPE) {
            mMandyStatusView.post(new Runnable() {
                @Override
                public void run() {
                    RecyclerView r = getRecyclerView();
                    r.setPadding(r.getPaddingLeft(), mMandyStatusView.getHeight(),
                            r.getPaddingRight(), r.getPaddingBottom());
                }
            });
        }

        for (MandyStatus.AospaProject aospaProject : status.mAospaProjects) {
            RepoView repoView = new RepoView();
            repoView.setStatus(status, aospaProject, this);
            addItem(repoView);
        }
    }

    private void stopFetching() {
        if (mMandyApi != null) {
            mMandyApi.disconnect();
        }
    }

    private void fail() {
        if (getActivity() != null) {
            Utils.toast(R.string.server_not_reachable, getActivity());
            mRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    int getLayout() {
        return R.layout.fragment_merge;
    }

    @Override
    int getRecyclerViewId() {
        return R.id.recyclerview;
    }

    @Override
    public void onMergePressed() {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.merge_confirm)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRefreshLayout.setRefreshing(true);
                        MandyApi mandyApi = new MandyApi("repo/merge", getActivity());
                        mandyApi.post(null, getUser().mApiToken, new MandyApi.ApiListener() {
                            @Override
                            public void onReturn(String output, int code) {
                                MandyStatus mandyStatus = new MandyStatus(output);
                                if (mandyStatus.valid()) {
                                    mandyStatus.cache(getActivity());
                                    setup(mandyStatus);

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
                }).show();
    }

    @Override
    public void onSubmittingPressed() {
        if (!getUser().mModerator && !getUser().mAdmin) {
            Utils.toast(R.string.mod_only, getActivity());
        }
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.submit_confirm)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRefreshLayout.setRefreshing(true);
                        MandyApi mandyApi = new MandyApi("repo/submit", getActivity());
                        mandyApi.post(null, getUser().mApiToken, new MandyApi.ApiListener() {
                            @Override
                            public void onReturn(String output, int code) {
                                MandyStatus mandyStatus = new MandyStatus(output);
                                if (mandyStatus.valid()) {
                                    mandyStatus.cache(getActivity());
                                    setup(mandyStatus);

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
                }).show();
    }

    @Override
    public void onRevertingPressed() {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.merge_confirm)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRefreshLayout.setRefreshing(true);
                        MandyApi mandyApi = new MandyApi("repo/revert", getActivity());
                        mandyApi.post(null, getUser().mApiToken, new MandyApi.ApiListener() {
                            @Override
                            public void onReturn(String output, int code) {
                                MandyStatus mandyStatus = new MandyStatus(output);
                                if (mandyStatus.valid()) {
                                    mandyStatus.cache(getActivity());
                                    setup(mandyStatus);

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
                }).show();
    }

    @Override
    public void onConflictChecked(final MandyStatus.AospaProject aospaProject, final boolean conflicted) {
        mRefreshLayout.setRefreshing(true);
        aospaProject.mConflicted = conflicted;

        MandyApi mandyApi = new MandyApi("repo/conflicted", getActivity());
        mandyApi.post(aospaProject.toString(), getUser().mApiToken, new MandyApi.ApiListener() {
            @Override
            public void onReturn(String output, int code) {
                MandyStatus mandyStatus = new MandyStatus(output);
                if (mandyStatus.valid()) {
                    mandyStatus.cache(getActivity());
                    setup(mandyStatus);

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

}
