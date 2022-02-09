package com.example.advancedprayertimes.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import com.example.advancedprayertimes.BuildConfig
import com.example.advancedprayertimes.R
import com.example.advancedprayertimes.databinding.ActivityTimeOverviewBinding
import com.example.advancedprayertimes.logic.AppEnvironment
import com.example.advancedprayertimes.logic.CustomLocation
import com.example.advancedprayertimes.logic.CustomPlaceEntity
import com.example.advancedprayertimes.logic.PrayerTimeEntity
import com.example.advancedprayertimes.logic.PrayerTimeEntity.Companion.getPrayerByTime
import com.example.advancedprayertimes.logic.db.DBHelper
import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import com.example.advancedprayertimes.logic.extensions.toStringByFormat
import com.example.advancedprayertimes.logic.util.DataManagementUtil
import com.example.advancedprayertimes.logic.util.HttpRequestUtil
import com.example.advancedprayertimes.logic.util.LocationUtil.RetrieveCityByLocation
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_time_overview.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class TimeOverviewActivity : AppCompatActivity()
{
    private val prayerTimeTypeWithAssociatedTextView = HashMap<EPrayerTimeType, TextView>()
    private var _placesClient: PlacesClient? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContentView(ActivityTimeOverviewBinding.inflate(layoutInflater).root)
        AppEnvironment.dbHelper = DBHelper(this.applicationContext)

        configurePrayerTimeTextViews()
        configureGooglePlacesAPI()

        load_prayer_times_button.setOnClickListener { asyncLoadPrayerTimes() }
        initiate_redrawing_of_prayer_graphic_button.setOnClickListener { doDebuggingStuff() }
        cacheButtonStuff.setOnClickListener { openCachyCache() }
    }

    private fun openCachyCache() {
        try {
            val myIntent = Intent(this, TimeCacheOverviewActivity::class.java)
            this.startActivity(myIntent)
        } catch (exception: Exception) {
            doErrorToastyToast(getExceptionDisplayText(exception))
        }
    }

    private fun doDebuggingStuff() {
        prayerTimeGraphicView.invalidate()
    }

    private fun getExceptionDisplayText(exception: Exception) : String {
        var displayText: String = exception.message ?: "ERROR"

        if (exception.cause != null) {
            displayText += "\n\n" + exception.cause!!.message
        }

        return displayText
    }

    /**
     * Display the provided error message in the UI
     */
    private fun doErrorToastyToast(message: String)
    {
        Handler(Looper.getMainLooper()).post {

            fajrTextLabel.maxLines = 50
            fajrTextLabel.maxHeight = 500
            fajrTextLabel.maxEms = 500

            // TODO: MAKE MULTILINE COMPATIBLE
            Snackbar.make(fajrTextLabel, message, Snackbar.LENGTH_LONG).show()

            //Toast.makeText(this.applicationContext, message, Toast.LENGTH_LONG).show()
            resetLoadingUIFeedback()
        }
    }

    /**
     * Retrieve prayer time information based on prayer time settings,
     * map the data correctly to the prayer time entities
     * and then apply the entity informations to the UI elements.
     */
    private fun loadPrayerTimes()
    {
        if (AppEnvironment.GetPrayerTimeSettingsByPrayerTimeTypeHashMap().isEmpty()) {
            doErrorToastyToast("There are no prayer time settings!")
            return
        }

        if (AppEnvironment.PlaceEntity?.location == null) {
            doErrorToastyToast("Location could not be retrieved!")
            return
        }

        try
        {
            val geoLocationAddress = RetrieveCityByLocation(
                this.applicationContext,
                AppEnvironment.PlaceEntity!!.location!!.longitude,
                AppEnvironment.PlaceEntity!!.location!!.latitude
            )

            retrieveTimeData(geoLocationAddress)
        }
        catch (exception: Exception) {
            doErrorToastyToast(getExceptionDisplayText(exception))
        }

        try
        {
            AppEnvironment.mapTimeDataToTimesEntities()
        }
        catch (exception: Exception) {
            doErrorToastyToast(getExceptionDisplayText(exception))
        }

        AppEnvironment.timeDate = LocalDateTime.now()

        Handler(Looper.getMainLooper()).post {
            syncTimeInformationToUserInterface()
            resetLoadingUIFeedback()
        }
    }

    private fun configureGooglePlacesAPI() {

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.GP_API_KEY, Locale.getDefault())
        }

        _placesClient = Places.createClient(applicationContext)

        val autocompleteSupportFragment = googlePlaceSearchAutoCompleteFragment as AutocompleteSupportFragment

        autocompleteSupportFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.LAT_LNG,
                Place.Field.NAME
            )
        )

        autocompleteSupportFragment.requireView().setBackgroundColor(Color.LTGRAY)
        autocompleteSupportFragment.requireView().setBackgroundResource(R.drawable.rounded_corner)

        val searchFieldEditText: AppCompatEditText =
            autocompleteSupportFragment.requireView().findViewById(R.id.places_autocomplete_search_input)
        val clearSearchFieldButton: AppCompatImageButton =
            autocompleteSupportFragment.requireView().findViewById(R.id.places_autocomplete_clear_button)

        searchFieldEditText.setTextColor(Color.BLACK)

        clearSearchFieldButton.viewTreeObserver.addOnGlobalLayoutListener {
            if (clearSearchFieldButton.visibility != View.GONE) {
                clearSearchFieldButton.visibility = View.GONE
            }
        }

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteSupportFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                try {
                    val cityAddress = RetrieveCityByLocation(
                        applicationContext,
                        place.latLng!!.longitude,
                        place.latLng!!.latitude
                    )
                    AppEnvironment.PlaceEntity = CustomPlaceEntity(cityAddress!!)
                    Toast.makeText(applicationContext, place.name, Toast.LENGTH_SHORT).show()
                } catch (exception: Exception) {
                    doErrorToastyToast("UnknownError - Place could not be retrieved!")
                }
            }

            override fun onError(status: Status) {
                doErrorToastyToast("Unknown Error")
            }
        })
    }

    override fun onPause() {

        val sharedPref = getSharedPreferences(AppEnvironment.GLOBAL_SHARED_PREFERENCE_NAME, MODE_PRIVATE)
        DataManagementUtil.SaveLocalData(sharedPref)
        super.onPause()
    }

    override fun onResume() {

        val sharedPref = getSharedPreferences(AppEnvironment.GLOBAL_SHARED_PREFERENCE_NAME, MODE_PRIVATE)
        DataManagementUtil.retrieveLocalData(sharedPref)
        syncTimeInformationToUserInterface()
        super.onResume()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configurePrayerTimeTextViews() {

        prayerTimeTypeWithAssociatedTextView[EPrayerTimeType.Fajr] = fajrTextLabel
        prayerTimeTypeWithAssociatedTextView[EPrayerTimeType.Duha] = duhaTextLabel
        prayerTimeTypeWithAssociatedTextView[EPrayerTimeType.Dhuhr] = dhuhrTextLabel
        prayerTimeTypeWithAssociatedTextView[EPrayerTimeType.Asr] = asrTextLabel
        prayerTimeTypeWithAssociatedTextView[EPrayerTimeType.Maghrib] = maghribTextLabel
        prayerTimeTypeWithAssociatedTextView[EPrayerTimeType.Isha] = ishaTextLabel

        for ((prayerTimeType, prayerTimeTextLabel) in prayerTimeTypeWithAssociatedTextView) {
            prayerTimeTextLabel.setOnClickListener { openSettingsForSpecificPrayerTimeType(prayerTimeType) }
        }

        for (prayerTimeType in EPrayerTimeType.values()) {

            val beginningTextView = getSpecificPrayerTimeTextView(prayerTimeType, EPrayerTimeMomentType.Beginning)
            val endTextView       = getSpecificPrayerTimeTextView(prayerTimeType, EPrayerTimeMomentType.End)
            
            beginningTextView!!.setOnTouchListener { view: View, event: MotionEvent ->
                singleTouchLongTouchHandler(
                    view,
                    event,
                    prayerTimeType,
                    true
                )
            }
            
            endTextView!!.setOnTouchListener { view: View, event: MotionEvent ->
                singleTouchLongTouchHandler(
                    view,
                    event,
                    prayerTimeType,
                    false
                )
            }
        }
    }

    private fun getSpecificPrayerTimeTextView(prayerTimeType: EPrayerTimeType, prayerPointInTimeType: EPrayerTimeMomentType): TextView? {

        return when (AbstractMap.SimpleEntry(prayerTimeType, prayerPointInTimeType)) {

            AbstractMap.SimpleEntry(EPrayerTimeType.Fajr, EPrayerTimeMomentType.Beginning) -> fajrTimeBeginningTextLabel
            AbstractMap.SimpleEntry(EPrayerTimeType.Fajr, EPrayerTimeMomentType.End) -> fajrTimeEndTextLabel

            AbstractMap.SimpleEntry(EPrayerTimeType.Duha, EPrayerTimeMomentType.Beginning) -> duhaTimeBeginningTextLabel
            AbstractMap.SimpleEntry(EPrayerTimeType.Duha, EPrayerTimeMomentType.End) -> duhaTimeEndTextLabel

            AbstractMap.SimpleEntry(EPrayerTimeType.Dhuhr, EPrayerTimeMomentType.Beginning) -> dhuhrTimeBeginningTextLabel
            AbstractMap.SimpleEntry(EPrayerTimeType.Dhuhr, EPrayerTimeMomentType.End) -> dhuhrTimeEndTextLabel

            AbstractMap.SimpleEntry(EPrayerTimeType.Asr, EPrayerTimeMomentType.Beginning) -> asrTimeBeginningTextLabel
            AbstractMap.SimpleEntry(EPrayerTimeType.Asr, EPrayerTimeMomentType.End) -> asrTimeEndTextLabel
            AbstractMap.SimpleEntry(EPrayerTimeType.Asr, EPrayerTimeMomentType.SubTimeOne) -> asrSubtimeOneTextLabel
            AbstractMap.SimpleEntry(EPrayerTimeType.Asr, EPrayerTimeMomentType.SubTimeTwo) -> asrSubtimeTwoTextLabel

            AbstractMap.SimpleEntry(EPrayerTimeType.Maghrib, EPrayerTimeMomentType.Beginning) -> maghribTimeBeginningTextLabel
            AbstractMap.SimpleEntry(EPrayerTimeType.Maghrib, EPrayerTimeMomentType.End) -> maghribTimeEndTextLabel
            AbstractMap.SimpleEntry(EPrayerTimeType.Maghrib, EPrayerTimeMomentType.SubTimeOne) -> maghribSubtimeOneTextLabel

            AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.Beginning) -> ishaTimeBeginningTextLabel
            AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.End) -> ishaTimeEndTextLabel
            AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.SubTimeOne) -> ishaSubtimeOneTextLabel
            AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.SubTimeTwo) -> ishaSubtimeTwoTextLabel
            AbstractMap.SimpleEntry(EPrayerTimeType.Isha, EPrayerTimeMomentType.SubTimeThree) -> ishaSubtimeThreeTextLabel

            else -> null
        }
    }

    private fun asyncLoadPrayerTimes() {
        Thread { loadPrayerTimes() }.start()
        load_prayer_times_button.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }

    var lastTouchBeginnTimePerTextViewHashMap: MutableMap<View, Long?> = HashMap()
    private fun singleTouchLongTouchHandler(
        textView: View,
        event: MotionEvent,
        prayerTimeType: EPrayerTimeType,
        isBeginning: Boolean
    ): Boolean {

        var dontPassEventOnToOtherListeners = false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> lastTouchBeginnTimePerTextViewHashMap[textView] = System.currentTimeMillis()
            MotionEvent.ACTION_UP -> if (lastTouchBeginnTimePerTextViewHashMap[textView] != null) {
                val milliSecondDifference =
                    System.currentTimeMillis() - lastTouchBeginnTimePerTextViewHashMap[textView]!!

                // long press on text view
                if (milliSecondDifference > 500) {
                    // to prevent the regular click event to trigger right after
                    dontPassEventOnToOtherListeners = true
                    val infoValuesText = "No settings"

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
                    Toast.makeText(applicationContext, infoValuesText, Toast.LENGTH_SHORT).show()
                }
            }
            else -> {}
        }
        return dontPassEventOnToOtherListeners
    }

    private fun openSettingsForSpecificPrayerTimeType(prayerTimeType: EPrayerTimeType) {
        try {
            val myIntent = Intent(this, PrayerSettingsActivity::class.java)
            myIntent.putExtra(INTENT_EXTRA, prayerTimeType) //Optional parameters
            this.startActivity(myIntent)
        } catch (exception: Exception) {
            doErrorToastyToast(getExceptionDisplayText(exception))
        }
    }

    /**
     * UI feedback that signifies a current loading process will be deactivated.
     */
    private fun resetLoadingUIFeedback() {
        load_prayer_times_button.isEnabled = true
        progressBar.visibility = View.INVISIBLE
    }

    private fun retrieveTimeData(cityAddress: Address?) {

        val toBeCalculatedPrayerTimes = AppEnvironment.GetPrayerTimeSettingsByPrayerTimeTypeHashMap()

        val timeZone = HttpRequestUtil.retrieveTimeZoneByLocation(
            cityAddress!!.longitude,
            cityAddress.latitude
        )

        val targetLocation = CustomLocation(cityAddress.longitude, cityAddress.latitude, timeZone)

        DataManagementUtil.retrieveDiyanetTimeData(toBeCalculatedPrayerTimes, cityAddress, checkBox.isChecked)
        DataManagementUtil.retrieveMuwaqqitTimeData(toBeCalculatedPrayerTimes, targetLocation, checkBox.isChecked)
        DataManagementUtil.retrieveAlAdhanTimeData(toBeCalculatedPrayerTimes, targetLocation, checkBox.isChecked)
    }

    /**
     * Heutiges Datum, gespeicherten Standort, Zeiten der Gebete und grafische Darstellung.
     */
    private fun syncTimeInformationToUserInterface() {
        try {

            displayedDateTextLabel.text = AppEnvironment.timeDate!!.toStringByFormat("dd.MM.yyyy")

            var cityName: String? = "-"

            if (AppEnvironment.PlaceEntity != null) {
                cityName = AppEnvironment.PlaceEntity!!.name
            }

            val editText: AppCompatEditText = googlePlaceSearchAutoCompleteFragment.requireView().findViewById(R.id.places_autocomplete_search_input)

            editText.setText(cityName)

            val defaultDisplayText: String = this.resources.getString(R.string.no_time_display_text);

            for (prayerEntity in PrayerTimeEntity.Prayers) {

                // Beginning
                val beginningText = prayerEntity.beginningTime?.toStringByFormat("HH:mm") ?: defaultDisplayText
                getSpecificPrayerTimeTextView(prayerEntity.prayerTimeType, EPrayerTimeMomentType.Beginning)?.text = beginningText

                // End
                val endText = prayerEntity.endTime?.toStringByFormat("HH:mm") ?: defaultDisplayText
                getSpecificPrayerTimeTextView(prayerEntity.prayerTimeType, EPrayerTimeMomentType.End)?.text = endText

                // SubTimeOne
                val subtime1EndText: String = prayerEntity.subtime1EndTime?.toStringByFormat("HH:mm") ?: defaultDisplayText
                getSpecificPrayerTimeTextView(prayerEntity.prayerTimeType, EPrayerTimeMomentType.SubTimeOne)?.text = subtime1EndText

                // SubTimeTwo
                val subtime2EndText: String = prayerEntity.subtime2EndTime?.toStringByFormat("HH:mm") ?: defaultDisplayText
                getSpecificPrayerTimeTextView(prayerEntity.prayerTimeType, EPrayerTimeMomentType.SubTimeTwo)?.text = subtime2EndText

                // SubTimeThree
                val subtime3EndText: String = prayerEntity.subtime3EndTime?.toStringByFormat("HH:mm") ?: defaultDisplayText
                getSpecificPrayerTimeTextView(prayerEntity.prayerTimeType, EPrayerTimeMomentType.SubTimeThree)?.text = subtime3EndText
            }

            prayerTimeGraphicView.displayPrayerEntity = getPrayerByTime(LocalTime.now())
            prayerTimeGraphicView.invalidate()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        var INTENT_EXTRA = "prayerTime"
    }
}