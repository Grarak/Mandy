package co.aospa.mandy.view.recyclerview;

import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.aospa.mandy.R;
import co.aospa.mandy.services.FirebaseNotification;
import co.aospa.mandy.utils.server.NotiActivities;
import co.aospa.mandy.utils.server.User;

/**
 * Created by willi on 19.04.17.
 */

public class ActivityView extends Item {

    private AppCompatTextView mDate;
    private AppCompatTextView mText;

    private User mUser;
    private NotiActivities.NotiActivity mNotiActivity;

    @Override
    public void onBind(View view) {
        mDate = (AppCompatTextView) view.findViewById(R.id.date);
        mText = (AppCompatTextView) view.findViewById(R.id.text);

        setup();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.view_activity, parent, false);
    }

    public void setNotiActivity(User user, NotiActivities.NotiActivity notiActivity) {
        mUser = user;
        mNotiActivity = notiActivity;
        setup();
    }

    private void setup() {
        if (mUser == null || mNotiActivity == null || mText == null) return;

        String message = FirebaseNotification.getMessage(mUser, mNotiActivity.mCode,
                mNotiActivity.mData.toString(), mText.getContext());
        mDate.setText(mNotiActivity.mDate);
        mText.setText(message == null ? " - " : message);
    }

}
