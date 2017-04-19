package co.aospa.mandy.view.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by willi on 16.04.17.
 */

public class Adapter extends RecyclerView.Adapter {

    private final List<Item> mItems;

    public Adapter(List<Item> items) {
        mItems = items;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mItems.get(position).onBind(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        return new RecyclerView.ViewHolder(mItems.get(position).onCreateView(
                LayoutInflater.from(parent.getContext()), parent)) {
        };
    }

}
