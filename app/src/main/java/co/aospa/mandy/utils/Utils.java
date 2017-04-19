package co.aospa.mandy.utils;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * Created by willi on 14.04.17.
 */

public class Utils {

    public static void toast(@StringRes int id, Context context) {
        toast(context.getString(id), context);
    }

    public static void toast(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static int getOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }

}
