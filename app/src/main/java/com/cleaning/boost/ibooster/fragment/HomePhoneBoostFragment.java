package com.cleaning.boost.ibooster.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.model.MemoryViewModel;
import com.cleaning.boost.ibooster.utils.Constants;
import com.cleaning.boost.ibooster.utils.Utils;
import com.gmc.libs.Formatter;
import com.gmc.libs.LogUtils;
import com.gmc.libs.SharedPrefUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomePhoneBoostFragment extends Fragment {

    private static HomePhoneBoostFragment homePhoneBoostFragment;

    @BindView(R.id.textViewMemoryPercent)
    TextView textViewMemoryPercent;
    @BindView(R.id.constraintLayoutMemory)
    ConstraintLayout constraintLayoutMemory;
    @BindView(R.id.textViewMemorySummary)
    TextView textViewMemorySummary;
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.imageView1)
    ImageView imageView1;
    @BindView(R.id.homeLayoutMemoryOptimized)
    ConstraintLayout homeLayoutMemoryOptimized;
    @BindView(R.id.homeLayoutMemory)
    LinearLayout homeLayoutMemory;

    private Unbinder unbinder;
    private MemoryViewModel memoryViewModel;

    public static HomePhoneBoostFragment getInstance() {
        if (homePhoneBoostFragment == null) homePhoneBoostFragment = new HomePhoneBoostFragment();
        return homePhoneBoostFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setLanguage(getContext(), null);
        memoryViewModel = ViewModelProviders.of(requireActivity()).get(MemoryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_phoneboost, container, false);
        unbinder = ButterKnife.bind(this, view);
        memoryViewModel.getName().observe(requireActivity(), this::resultReceived);
        homeLayoutMemory.setVisibility(View.VISIBLE);
        homeLayoutMemoryOptimized.setVisibility(View.GONE);
        long boostedTime = SharedPrefUtils.getLong(getContext(), Constants.SHARED_PREF_PHONE_BOOST_TIME, 0);
        if (System.currentTimeMillis() - boostedTime < Constants.TIME_SEPARATOR_BOOSTED) {
            homeLayoutMemory.setVisibility(View.GONE);
            homeLayoutMemoryOptimized.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        homeLayoutMemory.setVisibility(View.VISIBLE);
        homeLayoutMemoryOptimized.setVisibility(View.GONE);
        long boostedTime = SharedPrefUtils.getLong(getContext(), Constants.SHARED_PREF_PHONE_BOOST_TIME, 0);
        if (System.currentTimeMillis() - boostedTime < Constants.TIME_SEPARATOR_BOOSTED) {
            homeLayoutMemory.setVisibility(View.GONE);
            homeLayoutMemoryOptimized.setVisibility(View.VISIBLE);
        }
    }

    private void resultReceived(String result) {
        try {
            String[] res = result.split("-");
            long total = Long.parseLong(res[0]);
            long free = Long.parseLong(res[1]);
            long used = total - free;
            float ramPercent = used * 100 / total;
            int process = Math.round(ramPercent);
            if (process > 100) process = 100;

            String usedGB = Formatter.formatFileSizeSI(getContext(), used);
            String totalGB = Formatter.formatFileSizeSI(getContext(), total);
            String sum = "Used " + usedGB + " / " + totalGB;

            textViewMemoryPercent.setText(String.valueOf(process));
            textViewMemorySummary.setText(sum);
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unbind the view to free some memory
        unbinder.unbind();
    }
}
