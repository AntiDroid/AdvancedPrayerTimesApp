package com.example.advancedprayertimes.UI;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.advancedprayertimes.Logic.Enums.EPrayerPointInTimeType;
import com.example.advancedprayertimes.UI.Fragments.PrayerBeginningSettingsFragment;
import com.example.advancedprayertimes.UI.Fragments.PrayerEndSettingsFragment;

public class PrayerSettingsViewPagerAdapter extends FragmentStateAdapter
{
    private EPrayerPointInTimeType _beginningPointInTime = null;
    private EPrayerPointInTimeType _endPointInTime = null;

    public PrayerSettingsViewPagerAdapter(FragmentActivity fragmentActivity, EPrayerPointInTimeType beginningPointInTime, EPrayerPointInTimeType endPointInTime)
    {
        super(fragmentActivity);
        _beginningPointInTime = beginningPointInTime;
        _endPointInTime = endPointInTime;
    }

    @Override
    public Fragment createFragment(int position)
    {
        switch(position)
        {
            case 0:
                return PrayerBeginningSettingsFragment.newInstance(_beginningPointInTime);
            case 1:
                return PrayerEndSettingsFragment.newInstance(_endPointInTime);

            default:
                return null;
        }
    }

    @Override
    public int getItemCount()
    {

        return 2;
    }
}
