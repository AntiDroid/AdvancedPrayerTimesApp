package com.example.advancedprayertimes.UI.Activities;

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

import com.example.advancedprayertimes.BuildConfig;
import com.example.advancedprayertimes.Logic.AppEnvironment;
import com.example.advancedprayertimes.Logic.DB.DBHelper;
import com.example.advancedprayertimes.Logic.DataManagementUtil;
import com.example.advancedprayertimes.Logic.Entities.API_Entities.CustomPlaceEntity;
import com.example.advancedprayertimes.Logic.Entities.CustomLocation;
import com.example.advancedprayertimes.Logic.Entities.DayPrayerTimesPackageEntity;
import com.example.advancedprayertimes.Logic.Entities.PrayerTimeEntity;
import com.example.advancedprayertimes.Logic.Entities.Setting_Entities.PrayerSettingsEntity;
import com.example.advancedprayertimes.Logic.Entities.Setting_Entities.PrayerTimeBeginningEndSettingsEntity;
import com.example.advancedprayertimes.Logic.Entities.Setting_Entities.SubTimeSettingsEntity;
import com.example.advancedprayertimes.Logic.Enums.EHttpRequestMethod;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeMomentType;
import com.example.advancedprayertimes.Logic.Enums.EPrayerTimeType;
import com.example.advancedprayertimes.Logic.Enums.ESupportedAPIs;
import com.example.advancedprayertimes.Logic.HttpAPIRequestUtil;
import com.example.advancedprayertimes.Logic.LocationUtil;
import com.example.advancedprayertimes.R;
import com.example.advancedprayertimes.databinding.ActivityTimeOverviewBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TimeOverviewActivity extends AppCompatActivity
{
    private ActivityTimeOverviewBinding binding = null;
    private HashMap<EPrayerTimeType, TextView> prayerTimeTypeWithAssociatedTextView = new HashMap<>();

    PlacesClient _placesClient;

    private AutocompleteSupportFragment autocompleteSupportFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityTimeOverviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppEnvironment.dbHelper = new DBHelper(this.getApplicationContext());

        binding.loadPrayerTimesButton.setOnClickListener(view -> asyncLoadPrayerTimes());
        binding.initiateRedrawingOfPrayerGraphicButton.setOnClickListener(view ->
        {
            binding.prayerTimeGraphicView.invalidate();

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    //coolCoolStuff();
                }
            }).start();
        });

        configurePrayerTimeTextViews();
        configureGooglePlacesAPI();
    }

    private void coolCoolStuff()
    {
        //Toast.makeText(this.getApplicationContext(), "Test-Text by \nTalip\nTalip\nTalip\nTalip\nTalip", Toast.LENGTH_LONG).show();

        String urlText = "https://api.positionstack.com/v1/forward";

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("access_key", "42e6660aa35168f9e7860cf87c32e474");
        parameters.put("query", "Otto-Winter-Straße");

        StringBuilder responseContent = new StringBuilder();
        HttpAPIRequestUtil.RetrieveAPIFeedback(responseContent, urlText, EHttpRequestMethod.GET, parameters);

        String response = responseContent.toString();
    }

    private CustomPlaceEntity findPlaceFromGoogle(String placeID) throws JSONException
    {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("place_id", placeID);
        parameters.put("key", BuildConfig.GP_API_KEY);

        String urlText = "https://maps.googleapis.com/maps/api/place/details/json";

        StringBuilder response = new StringBuilder();
        int googlePlacesApiRequestStatus = HttpAPIRequestUtil.RetrieveAPIFeedback(response, urlText, EHttpRequestMethod.GET, parameters);

        if(googlePlacesApiRequestStatus == 0 && googlePlacesApiRequestStatus > 299)
        {
            return null;
        }

        JSONObject jsonBaseObj = new JSONObject(response.toString());

        if(jsonBaseObj.has("result"))
        {
            JSONObject jsonResultObj = new JSONObject(response.toString()).getJSONObject("result");

            if(jsonResultObj.has("geometry")
                    && jsonResultObj.has("place_id")
                    && jsonResultObj.has("name"))
            {
                JSONObject jsonGeometryObj = jsonResultObj.getJSONObject("geometry");

                if(jsonGeometryObj.has("location"))
                {
                    JSONObject jsonLocationObj = jsonResultObj.getJSONObject("location");

                    if(jsonLocationObj.has("lng") && jsonLocationObj.has("lat"))
                    {
                        String name = jsonResultObj.getString("name");
                        double longitude = jsonLocationObj.getDouble("lng");
                        double latitude = jsonLocationObj.getDouble("lat");

                        return new CustomPlaceEntity(placeID, latitude, longitude, name);
                    }
                }
            }
        }

        return null;
    }

    private void doErrorToastyToast(String message)
    {
        new Handler(Looper.getMainLooper()).post(() ->
        {
            Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_LONG).show();
            this.resetLoadingUIFeedback();
        });
    }

    StringBuilder errorMessage;

    public void loadPrayerTimes()
    {
        errorMessage = new StringBuilder();

        if(AppEnvironment.GetPrayerTimeSettingsByPrayerTimeTypeHashMap().size() == 0)
        {
            doErrorToastyToast("There are no prayer time settings!");
            return;
        }

        if(AppEnvironment.PlaceEntity == null || AppEnvironment.PlaceEntity.getLocation() == null)
        {
            doErrorToastyToast("Location could not be retrieved!");
            return;
        }

        try
        {
            Address geoLocationAddress = LocationUtil.RetrieveCityByLocation(this.getApplicationContext(), AppEnvironment.PlaceEntity.getLocation());
            retrieveTimeData(geoLocationAddress);
        }
        catch(Exception e)
        {
            String displayText = e.getMessage();

            if(e.getCause() != null)
            {
                displayText += "\n\n" + e.getCause().getMessage();
            }

            doErrorToastyToast(displayText);
        }

        try
        {
            mapTimeDataToTimesEntities();
        }
        catch(Exception e)
        {
            String displayText = e.getMessage();

            if(e.getCause() != null)
            {
                displayText += "\n\n" + e.getCause().getMessage();
            }

            doErrorToastyToast(displayText);
        }

        new Handler(Looper.getMainLooper()).post(() ->
        {
            syncTimeInformationToUserInterface();
            this.resetLoadingUIFeedback();
        });
    }

    private Location retrieveLocation()
    {
        Location targetLocation = new Location("");//provider name is unnecessary
        targetLocation.setLatitude(47.2820223);//your coords of course
        targetLocation.setLongitude(11.420481);

        return targetLocation;
    }

    private void configureGooglePlacesAPI()
    {
        if(!Places.isInitialized())
        {
            Places.initialize(getApplicationContext(), BuildConfig.GP_API_KEY, Locale.getDefault());
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
                CustomLocation targetLocation = new CustomLocation(place.getLatLng().longitude, place.getLatLng().latitude);

                try
                {
                    Address cityAddress = LocationUtil.RetrieveCityByLocation(getApplicationContext(), targetLocation);
                    AppEnvironment.PlaceEntity = new CustomPlaceEntity(cityAddress);

                    Toast.makeText(getApplicationContext(), place.getName(), Toast.LENGTH_SHORT).show();
                }
                catch(Exception e)
                {
                    Toast.makeText(getApplicationContext(), "Error - Place could not be retrieved!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@NonNull Status status)
            {
                AppEnvironment.PlaceEntity = null;
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause()
    {
        SharedPreferences sharedPref = this.getSharedPreferences(AppEnvironment.GLOBAL_SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        DataManagementUtil.SaveLocalData(sharedPref, binding);
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        SharedPreferences sharedPref = this.getSharedPreferences(AppEnvironment.GLOBAL_SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        DataManagementUtil.RetrieveLocalData(sharedPref, binding);
        this.syncTimeInformationToUserInterface();
        super.onResume();
    }

    private void configurePrayerTimeTextViews()
    {
        prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.Fajr, binding.fajrTextLabel);

        prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.Duha, binding.duhaTextLabel);

        prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.Dhuhr, binding.dhuhrTextLabel);

        prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.Asr, binding.asrTextLabel);

        prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.Maghrib, binding.maghribTextLabel);

        prayerTimeTypeWithAssociatedTextView.put(EPrayerTimeType.Isha, binding.ishaTextLabel);

        for(Map.Entry<EPrayerTimeType, TextView> entry : this.prayerTimeTypeWithAssociatedTextView.entrySet())
        {
            TextView prayerTimeTextLabel = entry.getValue();

            prayerTimeTextLabel.setOnClickListener(view -> openSettingsForSpecificPrayerTimeType(entry.getKey()));
        }

        for(EPrayerTimeType prayerTimeType : EPrayerTimeType.values())
        {
            TextView beginningTextView = getByPrayerTypeAndTimeType(prayerTimeType, EPrayerTimeMomentType.Beginning);
            TextView endTextView = getByPrayerTypeAndTimeType(prayerTimeType, EPrayerTimeMomentType.End);

            beginningTextView.setOnTouchListener((View view, MotionEvent event) -> doTouchStuff(view, event, prayerTimeType, true));
            endTextView.setOnTouchListener((View view, MotionEvent event) -> doTouchStuff(view, event, prayerTimeType, false));
        }
    }

    private TextView getByPrayerTypeAndTimeType(EPrayerTimeType prayerTimeType, EPrayerTimeMomentType prayerPointInTimeType)
    {
        switch(prayerTimeType)
        {
            case Fajr:
                switch(prayerPointInTimeType)
                {
                    case Beginning:
                        return binding.fajrTimeBeginningTextLabel;
                    case End:
                        return binding.fajrTimeEndTextLabel;
                }
                
            case Duha:
                switch(prayerPointInTimeType)
                {
                    case Beginning:
                        return binding.duhaTimeBeginningTextLabel;
                    case End:
                        return binding.duhaTimeEndTextLabel;
                }

            case Dhuhr:
                switch(prayerPointInTimeType)
                {
                    case Beginning:
                        return binding.dhuhrTimeBeginningTextLabel;
                    case End:
                        return binding.dhuhrTimeEndTextLabel;
                }
                
            case Asr:
                switch(prayerPointInTimeType)
                {
                    case Beginning:
                        return binding.asrTimeBeginningTextLabel;
                    case End:
                        return binding.asrTimeEndTextLabel;

                    case SubTimeOne:
                        return binding.asrSubtimeOneTextLabel;
                    case SubTimeTwo:
                        return binding.asrSubtimeTwoTextLabel;
                }
                
            case Maghrib:
                switch(prayerPointInTimeType)
                {
                    case Beginning:
                        return binding.maghribTimeBeginningTextLabel;
                    case End:
                        return binding.maghribTimeEndTextLabel;
                }
                
            case Isha:
                switch(prayerPointInTimeType)
                {
                    case Beginning:
                        return binding.ishaTimeBeginningTextLabel;
                    case End:
                        return binding.ishaTimeEndTextLabel;

                    case SubTimeOne:
                        return binding.ishaSubtimeOneTextLabel;
                    case SubTimeTwo:
                        return binding.ishaSubtimeTwoTextLabel;
                    case SubTimeThree:
                        return binding.ishaSubtimeThreeTextLabel;
                }
        }

        return null;
    }

    private void asyncLoadPrayerTimes()
    {
        Thread asyncRetrievePrayerTimesThread = new Thread(this::loadPrayerTimes);
        asyncRetrievePrayerTimesThread.start();

        binding.loadPrayerTimesButton.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    Map<View, Long> lastTouchBeginnTimePerTextViewHashMap = new HashMap<>();

    private boolean doTouchStuff(View textView, MotionEvent event, EPrayerTimeType prayerTimeType, boolean isBeginning)
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

                        String infoValuesText = "No settings";

                        // TODO: Fix
//                        if(AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.containsKey(prayerPointInTimeType))
//                        {
//                            PrayerTimeBeginningEndSettingsEntity settings = AppEnvironment.PrayerTimeSettingsByPrayerTimeTypeHashMap.get(prayerPointInTimeType);
//
//                            infoValuesText = "API:\n" + settings.get_api().toString()
//                                    + "\n\nMinute adjustment:\n" + settings.get_minuteAdjustment();
//
//                                    if(settings.getFajrCalculationDegree() != null)
//                                    {
//                                        infoValuesText += "\n\nFajr degree:\n" + settings.getFajrCalculationDegree();
//                                    }
//
//                                    if(settings.getIshaCalculationDegree() != null)
//                                    {
//                                        infoValuesText += "\n\nIsha degree:\n" + settings.getIshaCalculationDegree();
//                                    }
//                        }

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
            Intent myIntent = new Intent(TimeOverviewActivity.this, PrayerSettingsActivity.class);
            myIntent.putExtra(INTENT_EXTRA, prayerTimeType); //Optional parameters
            TimeOverviewActivity.this.startActivity(myIntent);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, DayPrayerTimesPackageEntity> muwaqqitTimesHashMap = new HashMap<>();
    Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, DayPrayerTimesPackageEntity> diyanetTimesHashMap = new HashMap<>();

    private void resetLoadingUIFeedback()
    {
        binding.loadPrayerTimesButton.setEnabled(true);
        binding.progressBar.setVisibility(View.INVISIBLE);
    }

    private void retrieveTimeData(Address cityAddress) throws Exception
    {
        Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimeBeginningEndSettingsEntity> toBeCalculatedPrayerTimes = AppEnvironment.GetPrayerTimeSettingsByPrayerTimeTypeHashMap();

        this.diyanetTimesHashMap = DataManagementUtil.RetrieveDiyanetTimeData(toBeCalculatedPrayerTimes, cityAddress);
        this.muwaqqitTimesHashMap = DataManagementUtil.RetrieveMuwaqqitTimeData(toBeCalculatedPrayerTimes, cityAddress);
    }

    private void mapTimeDataToTimesEntities()
    {
        for(PrayerTimeEntity prayerTimeEntity : PrayerTimeEntity.Prayers)
        {
            LocalDateTime beginningTime = getCorrectBeginningAndEndTime(prayerTimeEntity.getPrayerTimeType(), EPrayerTimeMomentType.Beginning);
            LocalDateTime endTime = getCorrectBeginningAndEndTime(prayerTimeEntity.getPrayerTimeType(), EPrayerTimeMomentType.End);

            LocalDateTime subtimeOneTime = getCorrectSubTime(prayerTimeEntity.getPrayerTimeType(), EPrayerTimeMomentType.SubTimeOne, prayerTimeEntity);
            LocalDateTime subtimeTwoTime = getCorrectSubTime(prayerTimeEntity.getPrayerTimeType(), EPrayerTimeMomentType.SubTimeTwo, prayerTimeEntity);
            LocalDateTime subtimeThreeTime = getCorrectSubTime(prayerTimeEntity.getPrayerTimeType(), EPrayerTimeMomentType.SubTimeThree, prayerTimeEntity);;

            prayerTimeEntity.setBeginningTime(beginningTime);
            prayerTimeEntity.setEndTime(endTime);

            prayerTimeEntity.setSubtime1BeginningTime(beginningTime);
            prayerTimeEntity.setSubtime1EndTime(subtimeOneTime);

            prayerTimeEntity.setSubtime2BeginningTime(subtimeOneTime);
            prayerTimeEntity.setSubtime2EndTime(subtimeTwoTime);

            prayerTimeEntity.setSubtime3BeginningTime(subtimeTwoTime);
            prayerTimeEntity.setSubtime3EndTime(subtimeThreeTime);
        }
    }

    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

    private LocalDateTime getCorrectBeginningAndEndTime(EPrayerTimeType prayerType, EPrayerTimeMomentType prayerTypeTimeType)
    {
        PrayerSettingsEntity prayerSettings = AppEnvironment.prayerSettingsByPrayerType.get(prayerType);

        if(prayerSettings != null
                && (prayerTypeTimeType == EPrayerTimeMomentType.Beginning || prayerTypeTimeType == EPrayerTimeMomentType.End))
        {
            PrayerTimeBeginningEndSettingsEntity prayerBeginningEndSettings = null;

            if(prayerTypeTimeType == EPrayerTimeMomentType.Beginning)
            {
                prayerBeginningEndSettings = prayerSettings.getBeginningSettings();
            }
            else if(prayerTypeTimeType == EPrayerTimeMomentType.End)
            {
                prayerBeginningEndSettings = prayerSettings.getEndSettings();
            }

            LocalDateTime correctTime = null;

            if(prayerBeginningEndSettings != null)
            {
                // TODO: Isha-Ende muss Fajr des  *Folgetages* sein!

                AbstractMap.SimpleEntry prayerTimeWithType = new AbstractMap.SimpleEntry(prayerType, prayerTypeTimeType);

                if (prayerBeginningEndSettings.get_api() == ESupportedAPIs.Muwaqqit
                        && muwaqqitTimesHashMap.containsKey(prayerTimeWithType)
                        && muwaqqitTimesHashMap.get(prayerTimeWithType) != null)
                {
                    correctTime = muwaqqitTimesHashMap.get(prayerTimeWithType).GetTimeByType(prayerType, prayerTypeTimeType);
                }
                else if (prayerBeginningEndSettings.get_api() == ESupportedAPIs.Diyanet
                        && diyanetTimesHashMap.containsKey(prayerTimeWithType)
                        && diyanetTimesHashMap.get(prayerTimeWithType) != null)
                {
                    correctTime = diyanetTimesHashMap.get(prayerTimeWithType).GetTimeByType(prayerType, prayerTypeTimeType);
                }

                if(correctTime != null)
                {
                    long minuteAdjustment = (long) prayerBeginningEndSettings.get_minuteAdjustment();

                    // minute adjustment
                    correctTime = correctTime.plusMinutes(minuteAdjustment);

                    return correctTime;
                }
            }
        }

        return null;
    }

    private LocalDateTime getCorrectSubTime(EPrayerTimeType prayerType, EPrayerTimeMomentType prayerTypeTimeType, PrayerTimeEntity prayerTimeEntity)
    {
        PrayerSettingsEntity prayerSettings = AppEnvironment.prayerSettingsByPrayerType.get(prayerType);

        if(prayerSettings != null
                && prayerTypeTimeType != EPrayerTimeMomentType.Beginning && prayerTypeTimeType != EPrayerTimeMomentType.End)
        {
            SubTimeSettingsEntity subTimeSettings = null;

            switch(prayerTypeTimeType)
            {
                case SubTimeOne:
                case SubTimeTwo:
                case SubTimeThree:
                    subTimeSettings = prayerSettings.getSubPrayer1Settings();
                    break;
            }

            if(subTimeSettings != null)
            {
                if(prayerType == EPrayerTimeType.Asr)
                {
                    if(prayerTypeTimeType == EPrayerTimeMomentType.SubTimeOne && subTimeSettings.isEnabled1())
                    {
                        return muwaqqitTimesHashMap.get(new AbstractMap.SimpleEntry(prayerType, prayerTypeTimeType)).getAsrMitlhaynTime();
                    }
                    else if(prayerTypeTimeType == EPrayerTimeMomentType.SubTimeTwo && subTimeSettings.isEnabled2())
                    {
                        return muwaqqitTimesHashMap.get(new AbstractMap.SimpleEntry(prayerType, prayerTypeTimeType)).getAsrKarahaTime();
                    }
                }
                else if(prayerType == EPrayerTimeType.Isha)
                {
                    if(prayerTimeEntity.getDuration() == 0 || PrayerTimeEntity.Prayers.get(4).getDuration() == 0)
                    {
                        return null;
                    }
                    long timeBetweenIshaBeginningAndMaghribEnd = ChronoUnit.MILLIS.between(PrayerTimeEntity.Prayers.get(4).getEndTime(), prayerTimeEntity.getBeginningTime());
                    long nightDuration = prayerTimeEntity.getDuration() + PrayerTimeEntity.Prayers.get(4).getDuration() + timeBetweenIshaBeginningAndMaghribEnd;

                    if(subTimeSettings.isEnabled1() && prayerTypeTimeType != EPrayerTimeMomentType.SubTimeThree)
                    {
                        long thirdOfNight = nightDuration / 3;

                        switch(prayerTypeTimeType)
                        {
                            case SubTimeOne:
                                return PrayerTimeEntity.Prayers.get(4).getBeginningTime().plus(thirdOfNight, ChronoField.MILLI_OF_DAY.getBaseUnit());
                            case SubTimeTwo:
                                return PrayerTimeEntity.Prayers.get(4).getBeginningTime().plus(2 * thirdOfNight, ChronoField.MILLI_OF_DAY.getBaseUnit());
                        }
                    }
                    else if(subTimeSettings.isEnabled2())
                    {
                        long halfOfNight = nightDuration / 2;

                        switch(prayerTypeTimeType)
                        {
                            case SubTimeThree:
                                return PrayerTimeEntity.Prayers.get(4).getBeginningTime().plus(halfOfNight, ChronoField.MILLI_OF_DAY.getBaseUnit());
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Heutiges Datum, gespeicherten Standort, Zeiten der Gebete und grafische Darstellung.
     */
    private void syncTimeInformationToUserInterface()
    {
        try
        {
            binding.displayedDateTextLabel.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDateTime.now()));

            String cityName = "-";

            if(AppEnvironment.PlaceEntity != null)
            {
                cityName = AppEnvironment.PlaceEntity.getName();
            }

            AppCompatEditText editText = autocompleteSupportFragment.getView().findViewById(R.id.places_autocomplete_search_input);
            editText.setText(cityName);

            for(PrayerTimeEntity prayerTimeEntity : PrayerTimeEntity.Prayers)
            {
                String beginningText = this.getResources().getString(R.string.no_time_display_text);
                String endText = this.getResources().getString(R.string.no_time_display_text);

                if(prayerTimeEntity.getBeginningTime() != null)
                {
                    beginningText = prayerTimeEntity.getBeginningTime().format(timeFormat);
                }

                if(prayerTimeEntity.getEndTime() != null)
                {
                    endText = prayerTimeEntity.getEndTime().format(timeFormat);
                }

                TextView beginningTimeTextView = this.getByPrayerTypeAndTimeType(prayerTimeEntity.getPrayerTimeType(), EPrayerTimeMomentType.Beginning);
                TextView endTimeTextView = this.getByPrayerTypeAndTimeType(prayerTimeEntity.getPrayerTimeType(), EPrayerTimeMomentType.End);

                beginningTimeTextView.setText(beginningText);
                endTimeTextView.setText(endText);

                // ##############################
                // ##############################

                // SubTimeOne
                String subtime1EndText = this.getResources().getString(R.string.no_time_display_text);

                if(prayerTimeEntity.getSubtime1EndTime() != null)
                {
                    subtime1EndText = prayerTimeEntity.getSubtime1EndTime().format(timeFormat);
                }

                TextView subTimeOneTextView = this.getByPrayerTypeAndTimeType(prayerTimeEntity.getPrayerTimeType(), EPrayerTimeMomentType.SubTimeOne);

                if(subTimeOneTextView != null)
                {
                    subTimeOneTextView.setText(subtime1EndText);
                }

                // SubTimeTwo
                String subtime2EndText = this.getResources().getString(R.string.no_time_display_text);

                if(prayerTimeEntity.getSubtime2EndTime() != null)
                {
                    subtime2EndText = prayerTimeEntity.getSubtime2EndTime().format(timeFormat);
                }

                TextView subTimeTwoTextView = this.getByPrayerTypeAndTimeType(prayerTimeEntity.getPrayerTimeType(), EPrayerTimeMomentType.SubTimeTwo);

                if(subTimeTwoTextView != null)
                {
                    subTimeTwoTextView.setText(subtime2EndText);
                }

                // SubTimeThree
                String subtime3EndText = this.getResources().getString(R.string.no_time_display_text);

                if(prayerTimeEntity.getSubtime3EndTime() != null)
                {
                    subtime3EndText = prayerTimeEntity.getSubtime3EndTime().format(timeFormat);
                }

                TextView subTimeThreeTextView = this.getByPrayerTypeAndTimeType(prayerTimeEntity.getPrayerTimeType(), EPrayerTimeMomentType.SubTimeThree);

                if(subTimeThreeTextView != null)
                {
                    subTimeThreeTextView.setText(subtime3EndText);
                }
            }

            LocalDateTime currentLocalDateTime = LocalDateTime.now();

            binding.prayerTimeGraphicView.setDisplayPrayerEntity(PrayerTimeEntity.GetPrayerByTime(currentLocalDateTime));
            binding.prayerTimeGraphicView.invalidate();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}