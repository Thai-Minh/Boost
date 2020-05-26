package com.cleaning.boost.ibooster.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cleaning.boost.ibooster.fragment.HomeJunkFragment;

public class JunkFilesPagerAdapter extends FragmentPagerAdapter {

    public JunkFilesPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return 1;
    }

    // Returns the fragment to display for that page
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return HomeJunkFragment.getInstance();
            default:
                return HomeJunkFragment.getInstance();
        }
    }
}
