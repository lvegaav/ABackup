package com.americavoice.backup.news.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

/**
 * Created by javier on 9/26/17.
 */

public class NewsTabsAdapter extends FragmentStatePagerAdapter {


    public NewsTabsAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        Log.v("Adapter news", "New tab item at  " + position);
        Fragment fragment = null;
        switch (position) {
            case 0:
            default:
                fragment = new NewsFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
            default:
                return "News feed";
        }
    }
}
