package co.aospa.mandy;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.firebase.iid.FirebaseInstanceId;

import co.aospa.mandy.fragments.BaseFragment;
import co.aospa.mandy.services.FirebaseIDService;
import co.aospa.mandy.utils.Utils;
import co.aospa.mandy.utils.server.MandyApi;
import co.aospa.mandy.utils.server.Status;
import co.aospa.mandy.utils.server.User;

/**
 * Created by willi on 14.04.17.
 */

public class MainActivity extends AppCompatActivity {

    private View mSignView;
    private View mSignInBtn;
    private View mSignUpBtn;

    private SignFragment mSignFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User user;
        if ((user = User.getCached(this)) != null && user.mVerified) {
            startNavigation(user);
            return;
        }

        setContentView(R.layout.activity_main);

        mSignView = findViewById(R.id.sign_view);
        (mSignInBtn = findViewById(R.id.signin_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignPressed(mSignFragment, true, v);
                mSignUpBtn.setEnabled(false);
            }
        });
        (mSignUpBtn = findViewById(R.id.signup_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignPressed(mSignFragment, false, v);
                mSignInBtn.setEnabled(false);
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        mSignFragment = (SignFragment) fragmentManager.findFragmentByTag("signFragment");
        if (mSignFragment == null) {
            mSignFragment = new SignFragment();
        }

        if (savedInstanceState != null && savedInstanceState.getBoolean("signViewVisible")) {
            mSignView.setVisibility(View.VISIBLE);
            if (mSignFragment.isSignin()) {
                mSignUpBtn.setEnabled(false);
            } else {
                mSignInBtn.setEnabled(false);
            }
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.sign_view,
                mSignFragment, "signFragment").commit();
    }

    private void onSignPressed(SignFragment signFragment, boolean signin, View button) {
        if (mSignView.getVisibility() == View.GONE) {
            signFragment.onSignPressed(signin, false, button, this);

            Animation animation = AnimationUtils.loadAnimation(this, R.anim.sign_bounce);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mSignView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mSignView.startAnimation(animation);
        } else {
            signFragment.onSignPressed(signin, true, button, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("signViewVisible", mSignView.getVisibility() == View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (mSignFragment.isPosting()) return;

        if (mSignView.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mSignView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mSignView.startAnimation(animation);

            mSignInBtn.setEnabled(true);
            mSignUpBtn.setEnabled(true);
        } else {
            super.onBackPressed();
        }
    }

    private void startNavigation(User user) {
        Intent intent = new Intent(this, NavigationActivity.class);
        intent.putExtra(NavigationActivity.USER_INTENT, user.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public static class SignFragment extends BaseFragment {

        private View mConfirmPasswordLayout;

        private AppCompatEditText mUsernameField;
        private AppCompatEditText mPasswordField;
        private AppCompatEditText mConfirmPasswordField;
        private View mProgress;
        private View mPressedBtn;

        private boolean mSignin;
        private boolean mPosting;

        private boolean mShowNotVerifiedDialog;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (mShowNotVerifiedDialog) {
                showShowNotVerifiedDialog();
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sign, container, false);

            mUsernameField = (AppCompatEditText) rootView.findViewById(R.id.username_field);
            mPasswordField = (AppCompatEditText) rootView.findViewById(R.id.password_field);
            mConfirmPasswordField = (AppCompatEditText) rootView.findViewById(R.id.confirm_password_field);
            mConfirmPasswordLayout = rootView.findViewById(R.id.confirm_password_layout);
            mProgress = rootView.findViewById(R.id.progress);

            updateLayout();

            return rootView;
        }

        private void updateLayout() {
            mConfirmPasswordLayout.setVisibility(mSignin ? View.GONE : View.VISIBLE);
            if (mPosting) {
                mUsernameField.setEnabled(false);
                mPasswordField.setEnabled(false);
                mConfirmPasswordField.setEnabled(false);
                mProgress.setVisibility(View.VISIBLE);
                if (mPressedBtn != null) {
                    mPressedBtn.setEnabled(false);
                }
            } else {
                mUsernameField.setEnabled(true);
                mPasswordField.setEnabled(true);
                mConfirmPasswordField.setEnabled(true);
                mProgress.setVisibility(View.INVISIBLE);
                if (mPressedBtn != null) {
                    mPressedBtn.setEnabled(true);
                }
            }
        }

        public void onSignPressed(final boolean signin, boolean sendRequest, View button, final MainActivity activity) {
            mSignin = signin;
            mPressedBtn = button;

            if (sendRequest) {
                String username = mUsernameField.getText().toString();
                String password = mPasswordField.getText().toString();
                String confirmPassword = mConfirmPasswordField.getText().toString();

                if (username.length() <= 3) {
                    Utils.toast(R.string.username_short, getActivity());
                    return;
                }

                if (password.length() <= 4) {
                    Utils.toast(R.string.password_short, getActivity());
                    return;
                }

                if (!signin && !password.equals(confirmPassword)) {
                    Utils.toast(R.string.password_invalid, getActivity());
                    return;
                }

                mPosting = true;
                updateLayout();

                MandyApi api = new MandyApi(signin ? "account/signin" : "account/signup", getActivity());

                String firebaseId = FirebaseInstanceId.getInstance().getToken();
                User user = new User(username, password, firebaseId);
                api.post(user.toString(), new MandyApi.ApiListener() {
                    @Override
                    public void onReturn(String output, int code) {
                        User user = new User(output);
                        if (user.valid()) {
                            if (signin) {
                                FirebaseIDService.sendToken(user);
                            }
                            user.cache(getActivity());
                            if (user.mVerified) {
                                activity.startNavigation(user);
                            } else {
                                showShowNotVerifiedDialog();
                            }

                            updateLayout();
                        } else {
                            Status status = new Status(output);
                            if (status.valid()) {
                                if (status.getCode() == Status.CODE_USERNAME_TAKEN) {
                                    Utils.toast(R.string.username_taken, getActivity());
                                } else if (status.getCode() == Status.CODE_USERNAME_PASSWORD_INVALID) {
                                    Utils.toast(R.string.signin_not_correct, getActivity());
                                }

                                updateLayout();
                            } else {
                                onFailure();
                            }
                        }
                    }

                    @Override
                    public void onFailure() {
                        Utils.toast(R.string.server_not_reachable, getActivity());
                        updateLayout();
                    }

                    private void updateLayout() {
                        mPosting = false;
                        SignFragment.this.updateLayout();
                    }
                });
            } else {
                updateLayout();
            }
        }

        public boolean isPosting() {
            return mPosting;
        }

        public boolean isSignin() {
            return mSignin;
        }

        private void showShowNotVerifiedDialog() {
            mShowNotVerifiedDialog = true;
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.not_verified))
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            mShowNotVerifiedDialog = false;
                        }
                    })
                    .show();
        }

    }

}
