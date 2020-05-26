package com.cleaning.boost.ibooster.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.cleaning.boost.ibooster.R;
import com.cleaning.boost.ibooster.utils.Constants;
import com.cleaning.boost.ibooster.utils.Utils;
import com.gmc.libs.SharedPrefUtils;

import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeCoolerFragment extends Fragment {

    private static HomeCoolerFragment homeCoolerFragment;

    @BindView(R.id.viewMiddleFragment)
    View viewMiddleFragment;
    @BindView(R.id.textViewTempCFragment)
    TextView textViewTempCFragment;
    @BindView(R.id.textViewTemperatureCFragment)
    TextView textViewTemperatureCFragment;
    @BindView(R.id.textViewTemperatureFFragment)
    TextView textViewTemperatureFFragment;
    @BindView(R.id.textViewTempFFrament)
    TextView textViewTempFFrament;

    private Unbinder unbinder;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
    private IntentFilter intentfilter;

    public static HomeCoolerFragment getInstance() {
        if (homeCoolerFragment == null) homeCoolerFragment = new HomeCoolerFragment();
        return homeCoolerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setLanguage(getContext(), null);
        intentfilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_cooler, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(broadcastreceiver, intentfilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastreceiver);
        bindData();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unbind the view to free some memory
        unbinder.unbind();
    }

    private void bindData() {
        // Neu thoi diem cooldown gan nhat trong vong khoang 20 phut thi show "... optimized!"
        long cleanedTime = SharedPrefUtils.getLong(getActivity(), Constants.SHARED_PREF_PHONE_COOL_DOWN_TIME, 0);
        long sub = System.currentTimeMillis() - cleanedTime;
        if (sub < Constants.TIME_SEPARATOR_COOLED) {

        }
    }

    private final HandleMessage handle = new HandleMessage();

    private class HandleMessage extends Handler {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == Constants.CPU_TEMPERATURE) {
                float c = msg.getData().getFloat("TEMP");
                float f = c * 9 / 5 + 32;

                textViewTemperatureCFragment.setText(numberFormat.format(c));
                textViewTemperatureFFragment.setText(numberFormat.format(f));
                String helthStatus;
                if (c < Constants.HEALTHY_CPU_TEMPERATURE_LOW) {
                    helthStatus = Constants.HEALTHY_CPU_TEMP_STATUS[0];
                } else if (c >= Constants.HEALTHY_CPU_TEMPERATURE_HIGH
                        && c < Constants.HEALTHY_CPU_TEMPERATURE_OVERHEATING) {
                    helthStatus = Constants.HEALTHY_CPU_TEMP_STATUS[2];
                } else if (c >= Constants.HEALTHY_CPU_TEMPERATURE_OVERHEATING) {
                    helthStatus = Constants.HEALTHY_CPU_TEMP_STATUS[3];
                } else {
                    helthStatus = Constants.HEALTHY_CPU_TEMP_STATUS[1];
                }
            }
        }
    }

    private final BroadcastReceiver broadcastreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float batteryTemp = (float) (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10;
            Bundle data = new Bundle();
            data.putFloat("TEMP", batteryTemp);
            Message msg = new Message();
            msg.what = Constants.CPU_TEMPERATURE;
            msg.setData(data);
            handle.sendMessage(msg);
        }
    };
}
