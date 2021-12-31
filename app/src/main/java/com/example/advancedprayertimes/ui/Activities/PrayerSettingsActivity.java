package com.example.advancedprayertimes.UI.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.Enums.EPrayerPointInTimeType;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.R;
import com.example.advancedprayertimes.UI.PrayerSettingsViewPagerAdapter;
import com.example.advancedprayertimes.databinding.PrayerSettingsActivityBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class PrayerSettingsActivity extends AppCompatActivity
{
    EPrayerTimeType _prayerTimeType;
    EPrayerPointInTimeType _prayerBeginningPointInTimeType;
    EPrayerPointInTimeType _prayerEndPointInTimeType;

    PrayerSettingsActivityBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prayer_settings_activity);

        binding = PrayerSettingsActivityBinding.inflate(getLayoutInflater());

        // Get the Intent that started this activity and extract the string
        Intent intent = this.getIntent();
        _prayerTimeType = (EPrayerTimeType) intent.getSerializableExtra(TimeOverviewActivity.INTENT_EXTRA);
        _prayerBeginningPointInTimeType = AppEnvironment.GetPointInTimeByPrayerType(_prayerTimeType, true);
        _prayerEndPointInTimeType = AppEnvironment.GetPointInTimeByPrayerType(_prayerTimeType, false);

        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(_prayerTimeType.toString() + " settings");
        }

        configureTabs();
    }

    ArrayList<String> tabNames = new ArrayList<String>()
    {
        {
            add("Beginning");
            add("End");
        }
    };

    private void configureTabs()
    {
        TabLayout tabManagerTabLayout = findViewById(R.id.tabLayoutTalip);
        ViewPager2 viewPager = findViewById(R.id.viewPagerTalip);

        PrayerSettingsViewPagerAdapter adapter = new PrayerSettingsViewPagerAdapter(this, _prayerBeginningPointInTimeType, _prayerEndPointInTimeType);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabManagerTabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy()
                {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position)
                    {
                        String tabTitle = "Unknown: " + position;

                        if(position < tabNames.size())
                        {
                            tabTitle = tabNames.get(position);
                        }

                        tab.setText(tabTitle);
                    }
                }).attach();
    }

    @Override
    protected void onStop()
    {

        super.onStop();
    }
}