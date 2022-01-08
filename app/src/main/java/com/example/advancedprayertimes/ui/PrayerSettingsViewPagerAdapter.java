package com.example.advancedprayertimes.UI;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.UI.Fragments.TabPrayerBeginningSettingsFragment;
import com.example.advancedprayertimes.UI.Fragments.TabPrayerEndSettingsFragment;
import com.example.advancedprayertimes.UI.Fragments.TabPrayerSpecificSettingsFragment;

public class PrayerSettingsViewPagerAdapter extends FragmentStateAdapter
{
    private EPrayerTimeType _prayerType;

    public PrayerSettingsViewPagerAdapter(FragmentActivity fragmentActivity, EPrayerTimeType prayerType)
    {
        super(fragmentActivity);
        this._prayerType = prayerType;
    }

    @Override
    public Fragment createFragment(int position)
    {
        switch(position)
        {
            case 0:
                return TabPrayerBeginningSettingsFragment.newInstance(_prayerType);
            case 1:
                return TabPrayerEndSettingsFragment.newInstance(_prayerType);
            case 2:
                return TabPrayerSpecificSettingsFragment.newInstance(_prayerType);

            default:
                return null;
        }
    }

    @Override
    public int getItemCount()
    {
        if(_prayerType == EPrayerTimeType.Asr || _prayerType == EPrayerTimeType.Isha)
        {
            return 3;
        }

        return 2;
    }
}
