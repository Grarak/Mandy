package co.aospa.mandy;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import co.aospa.mandy.fragments.ActivitiesFragment;
import co.aospa.mandy.fragments.AllUsersFragment;
import co.aospa.mandy.fragments.BaseFragment;
import co.aospa.mandy.fragments.MergeFragment;
import co.aospa.mandy.fragments.VerfiedUsersFragment;
import co.aospa.mandy.utils.server.MandyApi;
import co.aospa.mandy.utils.server.User;

public class NavigationActivity extends AppCompatActivity {

    public static final String USER_INTENT = "user";

    private static class FragmentItem {

        @StringRes
        int mStringId;
        @DrawableRes
        int mDrawableId;
        Class mFragmentClass;

        FragmentItem(@StringRes int stringId, @DrawableRes int drawableId,
                     Class fragmentClass) {
            mStringId = stringId;
            mDrawableId = drawableId;
            mFragmentClass = fragmentClass;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FragmentItem) {
                FragmentItem item2 = (FragmentItem) obj;
                return mFragmentClass == item2.mFragmentClass && mStringId == item2.mStringId;
            }

            return false;
        }
    }

    private static List<FragmentItem> sFragments = new ArrayList<>();

    static {
        sFragments.add(new FragmentItem(R.string.merge, R.drawable.ic_merge, MergeFragment.class));
        sFragments.add(new FragmentItem(R.string.verified_users, R.drawable.ic_user, VerfiedUsersFragment.class));
        sFragments.add(new FragmentItem(R.string.all_users, R.drawable.ic_user_alert, AllUsersFragment.class));
        sFragments.add(new FragmentItem(R.string.activities, R.drawable.ic_log, ActivitiesFragment.class));
        sFragments.add(new FragmentItem(R.string.checkout, R.drawable.ic_checkout, null));
        sFragments.add(new FragmentItem(R.string.delete_branches, R.drawable.ic_delete, null));
        sFragments.add(new FragmentItem(R.string.logout, R.drawable.ic_logout, null));
    }

    private DrawerLayout mDrawer;
    private int mCurrentItem;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                for (FragmentItem fragmentitem : sFragments) {
                    if (fragmentitem.mStringId == item.getItemId()) {
                        onItemSelected(fragmentitem);
                        break;
                    }
                }
                return true;
            }
        });

        if (savedInstanceState != null) {
            mCurrentItem = savedInstanceState.getInt("current_item", 0);
        }

        if (mUser == null) {
            mUser = User.getCached(this);
        }

        setUpNavigationItems(navigationView);
        onItemSelected(sFragments.get(mCurrentItem));
    }

    private void setUpNavigationItems(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < sFragments.size(); i++) {
            FragmentItem item = sFragments.get(i);
            MenuItem menuItem = menu.add(0, item.mStringId, 0, item.mStringId);
            menuItem.setIcon(item.mDrawableId);
            if (sFragments.get(i).mFragmentClass != null) {
                menuItem.setCheckable(true);
                if (i == mCurrentItem) {
                    menuItem.setChecked(true);
                }
            }
        }
    }

    private void onItemSelected(FragmentItem item) {
        if (item.mStringId == R.string.checkout) {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://grarak.com/mandy/api/v1/script/merge"));
            startActivity(i);

            return;
        } else if (item.mStringId == R.string.delete_branches) {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://grarak.com/mandy/api/v1/script/deletebranch"));
            startActivity(i);

            return;
        } else if (item.mStringId == R.string.logout) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.logout_confirm)
                    .setNegativeButton(R.string.no, null)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            User.deleteCache(NavigationActivity.this);

                            startActivity(new Intent(NavigationActivity.this, MainActivity.class));
                            finish();
                        }
                    }).show();
            return;
        }

        mCurrentItem = sFragments.indexOf(item);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment.instantiate(this, item.mFragmentClass.getName());

        Fragment fragment = fragmentManager.findFragmentByTag(item.mFragmentClass.getName());
        if (fragment == null) {
            fragment = Fragment.instantiate(this, item.mFragmentClass.getName());
        }
        fragmentManager.beginTransaction().replace(R.id.content_view, fragment,
                item.mFragmentClass.getName()).commitAllowingStateLoss();
        mDrawer.closeDrawer(GravityCompat.START);
    }

    public void updateUser() {
        final Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag(sFragments.get(mCurrentItem).mFragmentClass.getName());

        MandyApi mandyApi = new MandyApi("account/get", this);
        mandyApi.get(mUser.mApiToken, new MandyApi.ApiListener() {
            @Override
            public void onReturn(String output, int code) {
                User user = new User(output);
                if (user.valid()) {
                    user.cache(NavigationActivity.this);
                    mUser = user;

                    ((BaseFragment) fragment).onUserUpdate(mUser);
                } else {
                    ((BaseFragment) fragment).onUserUpdateFailure();
                }
            }

            @Override
            public void onFailure() {
                ((BaseFragment) fragment).onUserUpdateFailure();
            }
        });

        mUser.cache(this);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_item", mCurrentItem);
    }
}
