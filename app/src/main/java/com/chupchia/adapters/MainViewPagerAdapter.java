package com.chupchia.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.chupchia.fragments.FeedFragment;
import com.chupchia.fragments.NotificationFragment;
import com.chupchia.fragments.ProfileFragment;

public class MainViewPagerAdapter extends FragmentStateAdapter {
    
    public MainViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FeedFragment();
            case 1:
                return new NotificationFragment();
            case 2:
                return new ProfileFragment();
            default:
                return new FeedFragment();
        }
    }
    
    @Override
    public int getItemCount() {
        return 3;
    }
}
