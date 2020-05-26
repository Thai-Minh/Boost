package com.cleaning.boost.ibooster.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.model.MainFunction;
import com.cleaning.boost.ibooster.utils.Constants;
import com.gmc.libs.SharedPrefUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainFunctionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<MainFunction> mainFunctionList;
    private long cleanedTime;

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, MainFunction function);
    }

    public void setOnItemClickListener(final OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public MainFunctionAdapter(Context context, List<MainFunction> mainFunctionList) {
        this.mainFunctionList = mainFunctionList;
        cleanedTime = SharedPrefUtils.getLong(context, Constants.SHARED_PREF_CLEANED_TIME, 0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_function, parent, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemView.setLayoutParams(lp);
        return new FunctionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FunctionViewHolder) {
            FunctionViewHolder functionHolder = (FunctionViewHolder) holder;
            MainFunction function = mainFunctionList.get(position);
            functionHolder.btnFunction.setText(function.stringResId);
            functionHolder.btnFunction.setCompoundDrawablesWithIntrinsicBounds(0, function.drawableResId, 0, 0);
            functionHolder.btnFunction.setOnClickListener(view -> onItemClickListener.onItemClick(view, function));
            if (position == 0) {
                // Neu thoi diem clean gan nhat cach luc nay >= 3h thi show warning image
                if (System.currentTimeMillis() - cleanedTime >= Constants.TIME_SEPARATOR_CLEANED_MAX) {
                    functionHolder.imageViewTopOfFunction.setVisibility(View.VISIBLE);
                } else {
                    functionHolder.imageViewTopOfFunction.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mainFunctionList == null ? 0 : mainFunctionList.size();
    }

    public class FunctionViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.btnFunction)
        Button btnFunction;
        @BindView(R.id.imageViewTopOfFunction)
        ImageView imageViewTopOfFunction;

        public FunctionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }
}
