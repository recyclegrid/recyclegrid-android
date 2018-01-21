package com.recyclegrid.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.recyclegrid.app.AccountFragment;
import com.recyclegrid.app.ExploreVenuesFragment;
import com.recyclegrid.app.FriendsCheckinsFragment;
import com.recyclegrid.app.NotificationsFragment;

public class TabFragmentPagerAdapter extends FragmentPagerAdapter {

    public TabFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new ExploreVenuesFragment();
            case 1:
                return new FriendsCheckinsFragment();
            case 2:
                return new AccountFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() { return 3; }
}
