package com.cleaning.boost.ibooster.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.model.AppsListItem;
import com.gmc.libs.Formatter;
import com.gmc.libs.LogUtils;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.TaskViewHolder> {

    public static final CacheItemSizeComparator CACHE_ITEM_SIZE_COMPARATOR = new CacheItemSizeComparator();
    private final List<AppsListItem> mCacheItemList;
    private long cacheSize;
    private final Context context;

    public AppListAdapter(Context context, List<AppsListItem> cacheList) {
        this.context = context;
        mCacheItemList = cacheList;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_list, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        try {
            AppsListItem cacheItem = mCacheItemList.get(position);

            NumberFormat formatter = NumberFormat.getNumberInstance();
            formatter.setMaximumFractionDigits(1);

            holder.textViewAppName.setText(cacheItem.getApplicationName());
            holder.imageViewAppIcon.setImageDrawable(cacheItem.getApplicationIcon());
            if (cacheItem.getCacheSize() <= 0) {
                holder.textViewCacheSize.setText(R.string.empty);
            } else {
                String fileSize = Formatter.formatFileSizeSI(context, cacheItem.getCacheSize());
                holder.textViewCacheSize.setText(fileSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // LogUtils.e(cacheItem.getApplicationName() + ": " + fileSize + ": " + cacheItem.getApplicationIcon());
    }

    @Override
    public int getItemCount() {
        return mCacheItemList.size();
    }

    public void resetData() {
        if (mCacheItemList != null) mCacheItemList.clear();
    }

    public void addItem(AppsListItem cacheItem, boolean notifyDataSetChanged) {
        mCacheItemList.add(cacheItem);
        cacheSize += cacheItem.getCacheSize();
        if (notifyDataSetChanged) notifyDataSetChanged();
    }

    public long getCacheSize() {
        return cacheSize;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageViewAppIcon)
        ImageView imageViewAppIcon;
        @BindView(R.id.textViewAppName)
        TextView textViewAppName;
        @BindView(R.id.textViewCacheSize)
        TextView textViewCacheSize;

        TaskViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void sort(CacheItemSizeComparator comparator) {
        try {
            Collections.sort(mCacheItemList, comparator);
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }

        notifyDataSetChanged();
    }

    public static class CacheItemSizeComparator implements Comparator<AppsListItem> {

        @Override
        public int compare(AppsListItem lhs, AppsListItem rhs) {
            return Long.compare(rhs.getCacheSize(), lhs.getCacheSize());
        }
    }
}
