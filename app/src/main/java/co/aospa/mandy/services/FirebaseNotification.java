package co.aospa.mandy.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import co.aospa.mandy.BuildConfig;
import co.aospa.mandy.R;
import co.aospa.mandy.utils.server.MandyStatus;
import co.aospa.mandy.utils.server.User;

/**
 * Created by willi on 16.04.17.
 */

public class FirebaseNotification extends FirebaseMessagingService {

    private static final int NOTIFICATION_VERIFIED = 0;
    private static final int NOTIFICATION_NEW_TAG_FOUND = 1;
    private static final int NOTIFICATION_MERGEABLE = 2;
    private static final int NOTIFICATION_MERGING = 3;
    private static final int NOTIFICATION_MERGED = 4;
    private static final int NOTIFICATION_REVERTING = 5;
    private static final int NOTIFICATION_REVERTED = 6;
    private static final int NOTIFICATION_SUBMITTING = 7;
    private static final int NOTIFICATION_SUBMITTED = 8;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String code = remoteMessage.getData().get("code");
        String belongto = remoteMessage.getData().get("belongto");
        String data = remoteMessage.getData().get("data");

        User signedInUser = User.getCached(this);
        if (signedInUser == null || !signedInUser.valid()
                || !signedInUser.mName.equalsIgnoreCase(belongto)) {
            return;
        }

        String message;
        try {
            message = getMessage(signedInUser, Integer.parseInt(code), data, this);
        } catch (NumberFormatException ignored) {
            return;
        }

        if (message != null) {
            PackageManager pm = getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(message)
                            .setContentIntent(contentIntent)
                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(0, builder.build());
        }
    }

    public static String getMessage(User signedInUser, int code, String data, Context context) {
        String message = null;
        switch (code) {
            case NOTIFICATION_VERIFIED: {
                User user = new User(data);
                if (user.valid() && user.mVerified) {
                    message = context.getString(R.string.account_verified);
                }
            }
            break;
            case NOTIFICATION_NEW_TAG_FOUND: {
                if (signedInUser != null && signedInUser.valid()) {
                    MandyStatus mandyStatus = new MandyStatus(data);
                    if (mandyStatus.valid()) {
                        message = context.getString(R.string.new_tag_found, mandyStatus.mLatestTag);
                    }
                }
            }
            break;
            case NOTIFICATION_MERGEABLE: {
                if (signedInUser != null && signedInUser.valid()) {
                    MandyStatus mandyStatus = new MandyStatus(data);
                    if (mandyStatus.valid()) {
                        message = context.getString(R.string.tag_mergeable, mandyStatus.mLatestTag);
                    }
                }
            }
            break;
            case NOTIFICATION_MERGING: {
                if (signedInUser != null && signedInUser.valid()) {
                    MandyStatus mandyStatus = new MandyStatus(data);
                    if (mandyStatus.valid()) {
                        message = context.getString(R.string.start_merging,
                                mandyStatus.mMerger.mName, mandyStatus.mLatestTag);
                    }
                }
            }
            break;
            case NOTIFICATION_MERGED: {
                if (signedInUser != null && signedInUser.valid()) {
                    MandyStatus mandyStatus = new MandyStatus(data);
                    if (mandyStatus.valid()) {
                        message = context.getString(R.string.finished_merged, mandyStatus.mLatestTag);
                    }
                }
            }
            break;
            case NOTIFICATION_REVERTING: {
                if (signedInUser != null && signedInUser.valid()) {
                    MandyStatus mandyStatus = new MandyStatus(data);
                    if (mandyStatus.valid()) {
                        message = context.getString(R.string.start_reverting,
                                mandyStatus.mReverter.mName, mandyStatus.mLatestTag);
                    }
                }
            }
            break;
            case NOTIFICATION_REVERTED: {
                if (signedInUser != null && signedInUser.valid()) {
                    MandyStatus mandyStatus = new MandyStatus(data);
                    if (mandyStatus.valid()) {
                        message = context.getString(R.string.finished_reverted, mandyStatus.mLatestTag);
                    }
                }
            }
            break;
            case NOTIFICATION_SUBMITTING: {
                if (signedInUser != null && signedInUser.valid()) {
                    MandyStatus mandyStatus = new MandyStatus(data);
                    if (mandyStatus.valid()) {
                        message = context.getString(R.string.start_submitting,
                                mandyStatus.mSubmitter.mName, mandyStatus.mLatestTag);
                    }
                }
            }
            break;
            case NOTIFICATION_SUBMITTED: {
                if (signedInUser != null && signedInUser.valid()) {
                    MandyStatus mandyStatus = new MandyStatus(data);
                    if (mandyStatus.valid()) {
                        message = context.getString(R.string.finished_submitted,
                                mandyStatus.mLatestTag);
                    }
                }
            }
            break;
        }

        return message;
    }

}
