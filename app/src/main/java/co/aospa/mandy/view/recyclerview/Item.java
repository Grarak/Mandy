package co.aospa.mandy.view.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by willi on 16.04.17.
 */

public abstract class Item {

    public abstract void onBind(View view);

    public abstract View onCreateView(LayoutInflater inflater, ViewGroup parent);

}
