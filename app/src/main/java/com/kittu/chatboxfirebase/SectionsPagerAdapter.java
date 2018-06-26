package com.kittu.chatboxfirebase;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0: RequestFragment requestFragment=new RequestFragment();
            return requestFragment;
            case 1: ChatFragment chatFragment=new ChatFragment();
                return chatFragment;
            case 2: FriendsFragment friendsFragment=new FriendsFragment();
                return friendsFragment;
                default: return null;
        }
    }


    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0: return "REQUESTS";
            case 1:   return "CHATS";
            case 2: return "FRIENDS";
            default: return null;
        }
         }

    @Override
    public int getCount() {
        return 3;
    }
}
