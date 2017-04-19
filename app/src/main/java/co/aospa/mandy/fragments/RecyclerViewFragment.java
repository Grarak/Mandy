package co.aospa.mandy.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import co.aospa.mandy.view.recyclerview.Adapter;
import co.aospa.mandy.view.recyclerview.Item;

/**
 * Created by willi on 16.04.17.
 */

public abstract class RecyclerViewFragment extends BaseFragment {

    private Adapter mAdapter;
    private List<Item> mItems;
    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayout(), container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(getRecyclerViewId());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (mItems == null) {
            mItems = new ArrayList<>();
        }
        mAdapter = new Adapter(mItems);
        mRecyclerView.setAdapter(mAdapter);

        init(rootView);

        if (mItems.size() == 0) {
            new AsyncTask<Void, Void, Void>() {
                private List<Item> mItems;

                @Override
                protected Void doInBackground(Void... params) {
                    addItems((mItems = new ArrayList<>()));
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);

                    RecyclerViewFragment.this.mItems.addAll(mItems);
                    mAdapter.notifyDataSetChanged();
                }
            }.execute();
        }

        return rootView;
    }

    void init(View rootView) {
    }

    abstract void addItems(List<Item> items);

    void addItem(Item item) {
        mItems.add(item);
        mAdapter.notifyDataSetChanged();
    }

    void clearItems() {
        mItems.clear();
        mAdapter.notifyDataSetChanged();
    }

    RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    abstract
    @LayoutRes
    int getLayout();

    abstract
    @IdRes
    int getRecyclerViewId();

}
