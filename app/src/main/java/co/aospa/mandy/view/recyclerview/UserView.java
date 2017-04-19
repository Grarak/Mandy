package co.aospa.mandy.view.recyclerview;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import co.aospa.mandy.R;
import co.aospa.mandy.utils.server.User;

/**
 * Created by willi on 18.04.17.
 */

public class UserView extends Item {

    public interface UserListener {
        void onModeratorChanged(UserView userView, User user, boolean moderator);

        void onVerifiedChanged(UserView userView, User user, boolean verified);

        void onRemove(UserView userView, User user);
    }

    private AppCompatTextView mName;
    private AppCompatTextView mTitle;
    private AppCompatCheckBox mMod;
    private AppCompatCheckBox mVerified;
    private View mRemove;

    private User mSignedInUser;
    private User mUser;
    private UserListener mUserListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.view_user, parent, false);
    }

    @Override
    public void onBind(View view) {
        mName = (AppCompatTextView) view.findViewById(R.id.name);
        mTitle = (AppCompatTextView) view.findViewById(R.id.title);
        mMod = (AppCompatCheckBox) view.findViewById(R.id.mod_box);
        mVerified = (AppCompatCheckBox) view.findViewById(R.id.verify_box);
        mRemove = view.findViewById(R.id.remove_btn);

        setup();
    }

    public void setUsers(User signedInUser, User user, UserListener userListener) {
        mSignedInUser = signedInUser;
        mUser = user;
        mUserListener = userListener;
        setup();
    }

    private void setup() {
        if (mSignedInUser == null || mUser == null || mUserListener == null) return;
        if (mName == null || mTitle == null || mMod == null || mVerified == null || mRemove == null)
            return;

        mName.setText(mUser.mName);
        String title = mTitle.getContext().getString(R.string.user);
        if (mUser.mAdmin) {
            title = mTitle.getContext().getString(R.string.admin);
        } else if (mUser.mModerator) {
            title = mTitle.getContext().getString(R.string.moderator);
        }
        mTitle.setText(title);
        mMod.setVisibility(mSignedInUser.mAdmin && mUser.mVerified ? View.VISIBLE : View.GONE);

        mRemove.setVisibility((mSignedInUser.mAdmin || mSignedInUser.mModerator) && !mUser.mVerified ?
                View.VISIBLE : View.GONE);
        mRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mSignedInUser.mAdmin || mSignedInUser.mModerator) && !mUser.mVerified) {
                    mUserListener.onRemove(UserView.this, mUser);
                }
            }
        });

        mMod.setVisibility(mUser.mAdmin ? View.GONE : View.VISIBLE);
        mMod.setOnCheckedChangeListener(null);
        mMod.setChecked(mUser.mModerator);
        mMod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSignedInUser.mAdmin) {
                    mUserListener.onModeratorChanged(UserView.this, mUser, isChecked);
                } else {
                    mMod.setChecked(!isChecked);
                }
            }
        });

        mVerified.setVisibility(mUser.mAdmin ? View.GONE : View.VISIBLE);
        if (!mUser.mAdmin) {
            mVerified.setOnCheckedChangeListener(null);
            mVerified.setChecked(mUser.mVerified);
            mVerified.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (mSignedInUser.mAdmin) {
                        mUserListener.onVerifiedChanged(UserView.this, mUser, isChecked);
                    } else {
                        mVerified.setChecked(!isChecked);
                    }
                }
            });
        }
    }

}
