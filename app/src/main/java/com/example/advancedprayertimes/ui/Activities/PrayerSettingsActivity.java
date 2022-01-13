package com.example.advancedprayertimes.UI.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.R;
import com.example.advancedprayertimes.UI.PrayerSettingsViewPagerAdapter;
import com.example.advancedprayertimes.databinding.ActivityPrayerSettingsBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class PrayerSettingsActivity extends AppCompatActivity
{
    EPrayerTimeType _prayerType;

    ActivityPrayerSettingsBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_settings);

        binding = ActivityPrayerSettingsBinding.inflate(getLayoutInflater());

        // Get the Intent that started this activity and extract the string
        Intent intent = this.getIntent();
        _prayerType = (EPrayerTimeType) intent.getSerializableExtra(TimeOverviewActivity.INTENT_EXTRA);

        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(_prayerType.toString() + " settings");
        }

        configureTabs();
    }

    ArrayList<String> tabNames = new ArrayList<String>()
    {
        {
            add("Beginning");
            add("End");
            add("Special");
        }
    };

    private void configureTabs()
    {
        TabLayout tabManagerTabLayout = findViewById(R.id.tabLayoutTalip);
        ViewPager2 viewPager = findViewById(R.id.viewPagerTalip);

        PrayerSettingsViewPagerAdapter adapter = new PrayerSettingsViewPagerAdapter(this, _prayerType);
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