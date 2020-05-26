package com.cleaning.boost.ibooster.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.model.AppsListItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppGridAdapter extends RecyclerView.Adapter<AppGridAdapter.TaskViewHolder> {

    private final List<AppsListItem> mAppItemList;

    public AppGridAdapter(List<AppsListItem> cacheList) {
        mAppItemList = cacheList;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_grid, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        try {
            AppsListItem cacheItem = mAppItemList.get(position);

            holder.textViewAppName.setText(cacheItem.getApplicationName());
            holder.imageViewAppIcon.setImageDrawable(cacheItem.getApplicationIcon());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mAppItemList.size();
    }

    public void resetData() {
        if (mAppItemList != null) mAppItemList.clear();
    }

    public void addItem(AppsListItem cacheItem, boolean notifyDataSetChanged) {
        mAppItemList.add(cacheItem);
        if (notifyDataSetChanged) notifyDataSetChanged();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageViewAppIcon)
        ImageView imageViewAppIcon;
        @BindView(R.id.textViewAppName)
        TextView textViewAppName;

        TaskViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
