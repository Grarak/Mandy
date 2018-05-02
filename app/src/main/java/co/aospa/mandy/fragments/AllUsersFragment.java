package co.aospa.mandy.fragments;

import android.content.DialogInterface;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import co.aospa.mandy.NavigationActivity;
import co.aospa.mandy.R;
import co.aospa.mandy.utils.Utils;
import co.aospa.mandy.utils.server.MandyApi;
import co.aospa.mandy.utils.server.User;
import co.aospa.mandy.utils.server.Users;
import co.aospa.mandy.view.recyclerview.Item;
import co.aospa.mandy.view.recyclerview.UserView;

/**
 * Created by willi on 18.04.17.
 */

public class AllUsersFragment extends RecyclerViewFragment implements UserView.UserListener {

    private SwipeRefreshLayout mRefreshLayout;

    private MandyApi mMandyApi;

    @Override
    void init(View rootView) {
        super.init(rootView);

        mRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchUsers();
            }
        });
    }

    @Override
    void addItems(List<Item> items) {
        final Users users = Users.getCached(getUserPrefName(), getActivity());
        if (users != null && users.valid()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setup(users);
                }
            });
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

    String getApiPath() {
        return "user/get";
    }

    String getUserPrefName() {
        return "users";
    }

    private void setup(Users users) {
        clearItems();

        List<User> userslist = Arrays.asList(users.mUsers);
        Collections.sort(userslist, new Comparator<User>() {
            @Override
            public int compare(User user, User t1) {
                return user.mName.toLowerCase().compareTo(t1.mName.toLowerCase());
            }
        });
        for (User user : users.mUsers) {
            UserView userView = new UserView();
            userView.setUsers(getUser(), user, this);
            addItem(userView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        fetchUsers();
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

    private void fetchUsers() {
        mRefreshLayout.setRefreshing(true);
        ((NavigationActivity) getActivity()).updateUser();
    }

    @Override
    public void onUserUpdate(User user) {
        super.onUserUpdate(user);

        mMandyApi = new MandyApi(getApiPath(), getActivity());
        mMandyApi.get(user.mApiToken, new MandyApi.ApiListener() {
            @Override
            public void onReturn(String output, int code) {
                Users users = new Users(output, getUserPrefName());
                if (users.valid()) {
                    users.cache(getActivity());
                    setup(users);

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
        super.onUserUpdateFailure();

        fail();
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
    public void onModeratorChanged(final UserView userView, final User user, final boolean moderator) {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.mod_confirm)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userView.setUsers(getUser(), user, AllUsersFragment.this);
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRefreshLayout.setRefreshing(true);
                        user.mModerator = moderator;

                        mMandyApi = new MandyApi("user/moderator", getActivity());
                        mMandyApi.post(user.toString(), getUser().mApiToken, new MandyApi.ApiListener() {
                            @Override
                            public void onReturn(String output, int code) {
                                Users users = new Users(output, getUserPrefName());
                                if (users.valid()) {
                                    users.cache(getActivity());
                                    setup(users);

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
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        userView.setUsers(getUser(), user, AllUsersFragment.this);
                    }
                })
                .show();
    }

    @Override
    public void onVerifiedChanged(final UserView userView, final User user, final boolean verified) {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.verify_confirm)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userView.setUsers(getUser(), user, AllUsersFragment.this);
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRefreshLayout.setRefreshing(true);
                        user.mVerified = verified;

                        mMandyApi = new MandyApi("user/verify", getActivity());
                        mMandyApi.post(user.toString(), getUser().mApiToken, new MandyApi.ApiListener() {
                            @Override
                            public void onReturn(String output, int code) {
                                Users users = new Users(output, getUserPrefName());
                                if (users.valid()) {
                                    mRefreshLayout.setRefreshing(false);
                                    if (getApiPath().equals("user/getverified")) {
                                        fetchUsers();
                                    } else {
                                        users.cache(getActivity());
                                        setup(users);
                                    }
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
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        userView.setUsers(getUser(), user, AllUsersFragment.this);
                    }
                })
                .show();
    }

    @Override
    public void onRemove(UserView userView, final User user) {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.remove_confirm)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRefreshLayout.setRefreshing(true);

                        mMandyApi = new MandyApi("user/remove", getActivity());
                        mMandyApi.post(user.toString(), getUser().mApiToken, new MandyApi.ApiListener() {
                            @Override
                            public void onReturn(String output, int code) {
                                Users users = new Users(output, getUserPrefName());
                                if (users.valid()) {
                                    users.cache(getActivity());
                                    setup(users);

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
                })
                .show();
    }
}
