package com.example.advancedprayertimes;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;

import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.DataManagementUtil;
import com.example.advancedprayertimes.Logic.Entities.CustomPlaceEntity;
import com.example.advancedprayertimes.Logic.Entities.DayPrayerTimesEntity;
import com.example.advancedprayertimes.Logic.Entities.PrayerEntity;
import com.example.advancedprayertimes.Logic.Entities.PrayerTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.Logic.LocationUtil;
import com.example.advancedprayertimes.databinding.TimeOverviewActivityBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimeOverviewActivity extends AppCompatActivity
{
    private TimeOverviewActivityBinding binding = null;
    private HashMap<EPrayerTimeType, TextView> prayerTimeTypeWithAssociatedTextView = new HashMap<>();

    PlacesClient _placesClient;

    private AutocompleteSupportFragment autocompleteSupportFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = TimeOverviewActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loadPrayerTimesButton.setOnClickListener(view -> asyncLoadPrayerTimes());
        binding.initiateRedrawingOfPrayerGraphicButton.setOnClickListener(view -> binding.prayerTimeGraphicView.invalidate());

        configurePrayerTimeTextViews();
        configureGooglePlacesAPI();

        AppEnvironment.context = this;
    }

    private void configureGooglePlacesAPI()
    {
        if(!Places.isInitialized())
        {
            Places.initialize(getApplicationContext(), BuildConfig.GP_API_KEY);
        }

        _placesClient = Places.createClient(getApplicationContext());

        autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.googlePlaceSearchAutoCompleteFragment);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));

        AppCompatEditText searchFieldEditText = autocompleteSupportFragment.getView().findViewById(R.id.places_autocomplete_search_input);
        AppCompatImageButton clearSearchFieldButton = autocompleteSupportFragment.getView().findViewById(R.id.places_autocomplete_clear_button);
        autocompleteSupportFragment.getView().setBackgroundColor(Color.LTGRAY);
        autocompleteSupportFragment.getView().setBackgroundResource(R.drawable.rounded_corner);

        searchFieldEditText.setTextColor(Color.BLACK);
        clearSearchFieldButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                if(clearSearchFieldButton.getVisibility() != View.GONE)
                {
                    clearSearchFieldButton.setVisibility(View.GONE);
                }
            }
        });

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener()
        {
            @Override
            public void onPlaceSelected(@NonNull Place place)
            {
                AppEnvironment.place = new CustomPlaceEntity(place);
                Toast.makeText(getApplicationContext(), place.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull Status status)
            {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause()
    {
        SharedPreferences sharedPref = this.getPreferences(MODE_PRIVATE);
        DataManagementUtil.SaveLocalData(sharedPref, binding);
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        SharedPreferences sharedPref = this.getPreferences(MODE_PRIVATE);
        DataManagementUtil.RetrieveLocalData(sharedPref, binding, this.prayerTimeTypeWithAssociatedTextView.keySet());
        this.applyTimeSettingsToOverview();
        super.onResume();
    }

    private void configurePrayerTimeTextViews()
    {
        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.FajrBeginning, binding.fajrTimeBeginningTextLabel);
        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.FajrEnd, binding.fajrTimeEndTextLabel);

        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.DhuhrBeginning, binding.dhuhrTimeBeginningTextLabel);
        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.DhuhrEnd, binding.dhuhrTimeEndTextLabel);

        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.AsrBeginning, binding.asrTimeBeginningTextLabel);
        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.AsrEnd, binding.asrTimeEndTextLabel);

        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.MaghribBeginning, binding.maghribTimeBeginningTextLabel);
        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.MaghribEnd, binding.maghribTimeEndTextLabel);

        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.IshaBeginning, binding.ishaTimeBeginningTextLabel);
        this.prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.IshaEnd, binding.ishaTimeEndTextLabel);

        for(Map.Entry<EPrayerTimeType, TextView> entry : this.prayerTimeTypeWithAssociatedTextView.entrySet())
        {
            EPrayerTimeType prayerTimeType = entry.getKey();
            TextView prayerTimeTextLabel = entry.getValue();

            prayerTimeTextLabel.setOnClickListener(view -> openSettingsForSpecificPrayerTimeType(prayerTimeType));
            prayerTimeTextLabel.setOnTouchListener((View view, MotionEvent event) -> doTouchStuff(view, event));
        }
    }

    private void asyncLoadPrayerTimes()
    {
        Thread asyncRetrievePrayerTimesThread = new Thread(this::loadPrayerTimes);
        asyncRetrievePrayerTimesThread.start();

        binding.loadPrayerTimesButton.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    Map<View, Long> lastTouchBeginnTimePerTextViewHashMap = new HashMap<>();

    private boolean doTouchStuff(View textView, MotionEvent event)
    {
        boolean dontPassEventOnToOtherListeners = false;

        switch(event.getAction())
        {
            // a new touch was registered
            case MotionEvent.ACTION_DOWN:
                lastTouchBeginnTimePerTextViewHashMap.put(textView, System.currentTimeMillis());
                break;

            // a touch was aborted
            case MotionEvent.ACTION_UP:

                if(lastTouchBeginnTimePerTextViewHashMap.get(textView) != null)
                {
                    long milliSecondDifference = System.currentTimeMillis() - lastTouchBeginnTimePerTextViewHashMap.get(textView);

                    // long press on text view
                    if(milliSecondDifference > 500)
                    {
                        // to prevent the regular click event to trigger right after
                        dontPassEventOnToOtherListeners = true;

                        EPrayerTimeType prayerTimeType = this.prayerTimeTypeWithAssociatedTextView.entrySet().stream().filter(x -> x.getValue() == textView).findFirst().get().getKey();

                        String infoValuesText = "No settings";

                        if(AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.containsKey(prayerTimeType))
                        {
                            PrayerTimeSettingsEntity settings = AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.get(prayerTimeType);

                            infoValuesText = "API:\n" + settings.get_api().toString()
                                    + "\n\nMinute adjustment:\n" + settings.get_minuteAdjustment();

                                    if(settings.getFajrCalculationDegree() != null)
                                    {
                                        infoValuesText += "\n\nFajr degree:\n" + settings.getFajrCalculationDegree();
                                    }

                                    if(settings.getIshaCalculationDegree() != null)
                                    {
                                        infoValuesText += "\n\nIsha degree:\n" + settings.getIshaCalculationDegree();
                                    }
                        }

                        Toast.makeText(getApplicationContext(), infoValuesText, Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            default:
                break;
        }

        return dontPassEventOnToOtherListeners;
    }

    public static String INTENT_EXTRA = "prayerTime";

    private void openSettingsForSpecificPrayerTimeType(EPrayerTimeType prayerTimeType)
    {
        try
        {
            Intent myIntent = new Intent(TimeOverviewActivity.this, TimeSettingsActivity.class);
            myIntent.putExtra(INTENT_EXTRA, prayerTimeType); //Optional parameters
            TimeOverviewActivity.this.startActivity(myIntent);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    Map<EPrayerTimeType, DayPrayerTimesEntity> muwaqqitTimesHashMap = new HashMap<>();
    Map<EPrayerTimeType, DayPrayerTimesEntity> diyanetTimesHashMap = new HashMap<>();

    public void loadPrayerTimes()
    {
        try
        {
            if(AppEnvironment.place == null)
            {
                new Handler(Looper.getMainLooper())
                        .post(() ->
                                new AlertDialog.Builder(this)
                                        .setTitle("LOCATION NOT AVAILABLE")
                                        .setMessage("Location information is missing!")
                                        .show()
                        );

                new Handler(Looper.getMainLooper()).post(() ->
                {
                    this.resetLoadingUIFeedback();
                });
                return;
            }

            Location targetLocation = new Location("");//provider name is unnecessary
            targetLocation.setLatitude(AppEnvironment.place.getLatitude());//your coords of course
            targetLocation.setLongitude(AppEnvironment.place.getLongitude());

            if(AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.size() == 0)
            {
                new Handler(Looper.getMainLooper())
                        .post(() ->
                        {
                            new AlertDialog.Builder(this)
                                    .setTitle("NO SETTINGS AVAILABLE")
                                    .setMessage("There are no prayer time settings!")
                                    .show();
                            this.resetLoadingUIFeedback();
                        }
                    );
                return;
            }

            retrieveTimes(AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap, targetLocation);
            assignCorrectTimesToPrayers();

            new Handler(Looper.getMainLooper()).post(() ->
            {
                applyTimeSettingsToOverview();
                this.resetLoadingUIFeedback();
            });
        }
        catch (Exception e)
        {
            new Handler(Looper.getMainLooper()).post(() ->
            {
                this.resetLoadingUIFeedback();
            });
            e.printStackTrace();
        }
    }

    private void resetLoadingUIFeedback()
    {
        binding.loadPrayerTimesButton.setEnabled(true);
        binding.progressBar.setVisibility(View.INVISIBLE);
    }

    private void retrieveTimes(Map<EPrayerTimeType, PrayerTimeSettingsEntity> toBeCalculatedPrayerTimes, Location targetLocation) throws Exception
    {
        Address cityAddress = LocationUtil.RetrieveCityByLocation(this, targetLocation);

        this.diyanetTimesHashMap = DataManagementUtil.RetrieveDiyanetTimes(this, toBeCalculatedPrayerTimes, cityAddress);
        this.muwaqqitTimesHashMap = DataManagementUtil.RetrieveMuwaqqitTimes(toBeCalculatedPrayerTimes, targetLocation);
    }

    private void assignCorrectTimesToPrayers()
    {
        for(PrayerEntity prayerEntity : PrayerEntity.prayers)
        {
            Date beginningTime = getCorrectTime(prayerEntity.getBeginningTimeType());
            Date endTime = getCorrectTime(prayerEntity.getEndTimeType());

            prayerEntity.setBeginningTime(beginningTime);
            prayerEntity.setEndTime(endTime);
        }
    }

    private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm");

    private Date getCorrectTime(EPrayerTimeType prayerTimeType)
    {
        if(AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.containsKey(prayerTimeType))
        {
            PrayerTimeSettingsEntity settings = AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.get(prayerTimeType);

            Date correctTime = null;

            if(settings != null)
            {
                // TODO: Isha-Ende muss Fajr des  *Folgetages* sein!

                if (settings.get_api() == ESupportedAPIs.Muwaqqit
                        && muwaqqitTimesHashMap.containsKey(prayerTimeType)
                        && muwaqqitTimesHashMap.get(prayerTimeType) != null)
                {
                    correctTime = muwaqqitTimesHashMap.get(prayerTimeType).GetTimeByType(prayerTimeType);
                }
                else if (settings.get_api() == ESupportedAPIs.Diyanet
                        && diyanetTimesHashMap.containsKey(prayerTimeType)
                        && diyanetTimesHashMap.get(prayerTimeType) != null)
                {
                    correctTime = diyanetTimesHashMap.get(prayerTimeType).GetTimeByType(prayerTimeType);
                }
            }

            if(correctTime != null)
            {
                long minuteAdjustment = (long) settings.get_minuteAdjustment();

                // minute adjustment
                correctTime = new Time(correctTime.getTime() + (minuteAdjustment * 60 * 1000));

                return correctTime;
            }
        }

        return null;
    }

    private void applyTimeSettingsToOverview()
    {
        try
        {
            binding.displayedDateTextLabel.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now()));

            String cityName = "-";

            if(AppEnvironment.place != null)
            {
                cityName = AppEnvironment.place.getName();
            }

            AppCompatEditText editText = autocompleteSupportFragment.getView().findViewById(R.id.places_autocomplete_search_input);
            editText.setText(cityName);

            for(PrayerEntity prayerEntity : PrayerEntity.prayers)
            {
                String beginningText = this.getResources().getString(R.string.no_time_display_text);
                String endText = this.getResources().getString(R.string.no_time_display_text);

                if(prayerEntity.getBeginningTime() != null)
                {
                    beginningText = timeFormat.format(prayerEntity.getBeginningTime().getTime());
                }

                if(prayerEntity.getEndTime() != null)
                {
                    endText = timeFormat.format(prayerEntity.getEndTime().getTime());
                }

                TextView beginningTimeTextView = this.prayerTimeTypeWithAssociatedTextView.get(prayerEntity.getBeginningTimeType());
                TextView endTimeTextView = this.prayerTimeTypeWithAssociatedTextView.get(prayerEntity.getEndTimeType());

                beginningTimeTextView.setText(beginningText);
                endTimeTextView.setText(endText);
            }

            LocalDateTime currentDate = LocalDateTime.now();
            Time currentTime = new Time(currentDate.getHour(), currentDate.getMinute(), currentDate.getSecond());

            binding.prayerTimeGraphicView.setDisplayPrayerEntity(PrayerEntity.GetPrayerByTime(currentTime));
            binding.prayerTimeGraphicView.invalidate();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}