package se.forskningsavd.check;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
 
    private final int PAGES = 3;
 
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new EditFragment();
            case 2:
                return new HistoryFragment();
            default:
                throw new IllegalArgumentException("The item position should be less or equal to:" + PAGES);
        }
    }
 
    @Override
    public int getCount() {
        return PAGES;
    }
}