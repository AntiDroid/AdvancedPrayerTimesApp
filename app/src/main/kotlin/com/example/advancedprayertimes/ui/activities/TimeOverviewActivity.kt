package com.example.advancedprayertimes.ui.activities

import com.example.advancedprayertimes.logic.LocationUtil.RetrieveCityByLocation
import com.example.advancedprayertimes.logic.PrayerTimeEntity.Companion.getPrayerByTime
import androidx.appcompat.app.AppCompatActivity
import com.example.advancedprayertimes.logic.enums.EPrayerTimeType
import android.widget.TextView
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import android.os.Bundle
import com.example.advancedprayertimes.logic.AppEnvironment
import com.example.advancedprayertimes.logic.db.DBHelper
import kotlin.Throws
import org.json.JSONException
import com.example.advancedprayertimes.logic.CustomPlaceEntity
import com.example.advancedprayertimes.logic.enums.EHttpResponseStatusType
import com.example.advancedprayertimes.logic.HttpAPIRequestUtil
import com.example.advancedprayertimes.logic.enums.EHttpRequestMethod
import org.json.JSONObject
import android.os.Looper
import android.widget.Toast
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.example.advancedprayertimes.logic.DataManagementUtil
import com.example.advancedprayertimes.logic.enums.EPrayerTimeMomentType
import android.view.MotionEvent
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Location
import android.os.Handler
import android.view.View
import com.example.advancedprayertimes.BuildConfig
import com.example.advancedprayertimes.R
import com.example.advancedprayertimes.databinding.ActivityTimeOverviewBinding
import com.example.advancedprayertimes.logic.api_entities.PrayerTimePackageAbstractClass
import com.example.advancedprayertimes.logic.setting_entities.PrayerTimeBeginningEndSettingsEntity
import com.example.advancedprayertimes.logic.CustomLocation
import com.example.advancedprayertimes.logic.PrayerTimeEntity
import com.example.advancedprayertimes.logic.enums.ESupportedAPIs
import com.example.advancedprayertimes.logic.setting_entities.SubTimeSettingsEntity
import com.google.android.gms.common.api.Status
import kotlinx.android.synthetic.main.activity_time_overview.*
import java.lang.Exception
import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.*

class TimeOverviewActivity : AppCompatActivity()
{
    private val prayerTimeTypeWithAssociatedTextView = HashMap<EPrayerTimeType, TextView>()

    private var _placesClient: PlacesClient? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContentView(ActivityTimeOverviewBinding.inflate(getLayoutInflater()).root)
        AppEnvironment.dbHelper = DBHelper(this.applicationContext)

        load_prayer_times_button.setOnClickListener { asyncLoadPrayerTimes() }

        initiate_redrawing_of_prayer_graphic_button.setOnClickListener {
            prayerTimeGraphicView.invalidate()
            val today = Calendar.getInstance()

            //prayTime.getPrayerTimes(today, 47.2820223, 11.420481, 1);
            Thread(Runnable {
//                    try
//                    {
//                        AlAdhanPrayerTimeDayEntity dayPrayerTimesPackageEntity = HttpAPIRequestUtil.RetrieveAlAdhanTimes(AppEnvironment.PlaceEntity.getLocation());
//
//                        String lol = dayPrayerTimesPackageEntity.getAsrTime().toString();
//                    }
//                    catch(Exception e)
//                    {
//                        e.printStackTrace();
//                    }
            }).start()
        }

        configurePrayerTimeTextViews()
        configureGooglePlacesAPI()
    }

    @Throws(JSONException::class)
    private fun findPlaceFromGoogle(placeID: String): CustomPlaceEntity? {

        val parameters = hashMapOf(
            "place_id" to placeID,
            "key" to BuildConfig.GP_API_KEY,
        )

        val urlText = "https://maps.googleapis.com/maps/api/place/details/json"
        val response = StringBuilder()

        var googlePlacesApiRequestStatus = EHttpResponseStatusType.None

        try
        {
            googlePlacesApiRequestStatus = HttpAPIRequestUtil.retrieveAPIFeedback(
                response,
                urlText,
                EHttpRequestMethod.GET,
                parameters
            )
        }
        catch (e: Exception)
        {
            // DO STUFF
        }

        if (googlePlacesApiRequestStatus != EHttpResponseStatusType.Success)
        {
            return null
        }

        val jsonBaseObj = JSONObject(response.toString())
        val jsonResultObj = jsonBaseObj.getJSONObject("result")
        val jsonGeometryObj = jsonResultObj.getJSONObject("geometry")
        val jsonLocationObj = jsonGeometryObj.getJSONObject("location")
        val name = jsonResultObj.getString("name")
        val longitude = jsonLocationObj.getDouble("lng")
        val latitude = jsonLocationObj.getDouble("lat")

        return CustomPlaceEntity(placeID, latitude, longitude, name)
    }

    private fun doErrorToastyToast(message: String?)
    {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this.applicationContext, message, Toast.LENGTH_LONG).show()
            resetLoadingUIFeedback()
        }
    }

    var errorMessage: StringBuilder? = null

    private fun loadPrayerTimes()
    {
        errorMessage = StringBuilder()

        if (AppEnvironment.GetPrayerTimeSettingsByPrayerTimeTypeHashMap().isEmpty())
        {
            doErrorToastyToast("There are no prayer time settings!")
            return
        }

        if (AppEnvironment.PlaceEntity?.location == null)
        {
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
        catch (e: Exception)
        {
            var displayText = e.message

            if (e.cause != null)
            {
                displayText += "\n\n" + e.cause!!.message
            }

            doErrorToastyToast(displayText)
        }

        try
        {
            mapTimeDataToTimesEntities()
        }
        catch (e: Exception)
        {
            var displayText = e.message

            if (e.cause != null)
            {
                displayText += "\n\n" + e.cause!!.message
            }

            doErrorToastyToast(displayText)
        }

        Handler(Looper.getMainLooper()).post {
            syncTimeInformationToUserInterface()
            resetLoadingUIFeedback()
        }
    }

    private fun retrieveLocation(): Location {
        val targetLocation = Location("")
        targetLocation.latitude = 47.2820223 //your coords of course
        targetLocation.longitude = 11.420481

        return targetLocation
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

//        val searchFieldEditText: AppCompatEditText =
//            autocompleteSupportFragment.requireView().findViewById(R.id.places_autocomplete_search_input)
//        val clearSearchFieldButton: AppCompatImageButton = autocompleteSupportFragment.requireView()
//            .findViewById(R.id.places_autocomplete_clear_button)

        autocompleteSupportFragment.requireView().setBackgroundColor(Color.LTGRAY)
        autocompleteSupportFragment.requireView().setBackgroundResource(R.drawable.rounded_corner)

//        searchFieldEditText.setTextColor(Color.BLACK)
//
//        clearSearchFieldButton.viewTreeObserver.addOnGlobalLayoutListener {
//            if (clearSearchFieldButton.visibility != View.GONE) {
//                clearSearchFieldButton.visibility = View.GONE
//            }
//        }

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
                } catch (e: Exception) {
                    Toast.makeText(
                        applicationContext,
                        "UnknownError - Place could not be retrieved!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onError(status: Status) {
                AppEnvironment.PlaceEntity = null
                Toast.makeText(applicationContext, "UnknownError", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onPause() {
        val sharedPref =
            getSharedPreferences(AppEnvironment.GLOBAL_SHARED_PREFERENCE_NAME, MODE_PRIVATE)
        DataManagementUtil.SaveLocalData(sharedPref, displayedDateTextLabel.text.toString())
        super.onPause()
    }

    override fun onResume() {
        val sharedPref =
            getSharedPreferences(AppEnvironment.GLOBAL_SHARED_PREFERENCE_NAME, MODE_PRIVATE)
        DataManagementUtil.RetrieveLocalData(sharedPref, displayedDateTextLabel)
        syncTimeInformationToUserInterface()
        super.onResume()
    }

    private fun configurePrayerTimeTextViews() {
        prayerTimeTypeWithAssociatedTextView[EPrayerTimeType.Fajr] = fajrTextLabel
        prayerTimeTypeWithAssociatedTextView[EPrayerTimeType.Duha] = duhaTextLabel
        prayerTimeTypeWithAssociatedTextView[EPrayerTimeType.Dhuhr] = dhuhrTextLabel
        prayerTimeTypeWithAssociatedTextView[EPrayerTimeType.Asr] = asrTextLabel
        prayerTimeTypeWithAssociatedTextView[EPrayerTimeType.Maghrib] = maghribTextLabel
        prayerTimeTypeWithAssociatedTextView[EPrayerTimeType.Isha] = ishaTextLabel

        for ((key, prayerTimeTextLabel) in prayerTimeTypeWithAssociatedTextView) {
            prayerTimeTextLabel.setOnClickListener { _: View? ->
                openSettingsForSpecificPrayerTimeType(
                    key
                )
            }
        }

        for (prayerTimeType in EPrayerTimeType.values()) {
            val beginningTextView =
                getByPrayerTypeAndTimeType(prayerTimeType, EPrayerTimeMomentType.Beginning)
            val endTextView = getByPrayerTypeAndTimeType(prayerTimeType, EPrayerTimeMomentType.End)
            beginningTextView!!.setOnTouchListener { view: View, event: MotionEvent ->
                doTouchStuff(
                    view,
                    event,
                    prayerTimeType,
                    true
                )
            }
            
            endTextView!!.setOnTouchListener { view: View, event: MotionEvent ->
                doTouchStuff(
                    view,
                    event,
                    prayerTimeType,
                    false
                )
            }
        }
    }

    private fun getByPrayerTypeAndTimeType(
        prayerTimeType: EPrayerTimeType,
        prayerPointInTimeType: EPrayerTimeMomentType
    ): TextView? {

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
        val asyncRetrievePrayerTimesThread = Thread { loadPrayerTimes() }
        asyncRetrievePrayerTimesThread.start()
        load_prayer_times_button.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }

    var lastTouchBeginnTimePerTextViewHashMap: MutableMap<View, Long?> = HashMap()
    private fun doTouchStuff(
        textView: View,
        event: MotionEvent,
        prayerTimeType: EPrayerTimeType,
        isBeginning: Boolean
    ): Boolean {

        var dontPassEventOnToOtherListeners = false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> lastTouchBeginnTimePerTextViewHashMap[textView] =
                System.currentTimeMillis()
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
            val myIntent = Intent(this@TimeOverviewActivity, PrayerSettingsActivity::class.java)
            myIntent.putExtra(INTENT_EXTRA, prayerTimeType) //Optional parameters
            this@TimeOverviewActivity.startActivity(myIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var diyanetTimesHashMap: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass?> =
        HashMap()
    var muwaqqitTimesHashMap: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass?> =
        HashMap()
    var alAdhanTimesHashMap: Map<Map.Entry<EPrayerTimeType, EPrayerTimeMomentType>, PrayerTimePackageAbstractClass?> =
        HashMap()

    private fun resetLoadingUIFeedback() {
        load_prayer_times_button.isEnabled = true
        progressBar.visibility = View.INVISIBLE
    }

    @Throws(Exception::class)
    private fun retrieveTimeData(cityAddress: Address?) {

        val toBeCalculatedPrayerTimes = AppEnvironment.GetPrayerTimeSettingsByPrayerTimeTypeHashMap()

        val timeZone = HttpAPIRequestUtil.RetrieveTimeZoneByLocation(
            cityAddress!!.longitude,
            cityAddress.latitude
        )

        val targetLocation = CustomLocation(cityAddress.longitude, cityAddress.latitude, timeZone)

        diyanetTimesHashMap = DataManagementUtil.RetrieveDiyanetTimeData(toBeCalculatedPrayerTimes, cityAddress)
        muwaqqitTimesHashMap = DataManagementUtil.RetrieveMuwaqqitTimeData(toBeCalculatedPrayerTimes, targetLocation)
        alAdhanTimesHashMap = DataManagementUtil.RetrieveAlAdhanTimeData(toBeCalculatedPrayerTimes, targetLocation)
    }

    private fun mapTimeDataToTimesEntities() {
        for (prayerTimeEntity in PrayerTimeEntity.Prayers) {

            val beginningTime = getCorrectBeginningAndEndTime(
                prayerTimeEntity.prayerTimeType,
                EPrayerTimeMomentType.Beginning
            )

            val endTime = getCorrectBeginningAndEndTime(
                prayerTimeEntity.prayerTimeType,
                EPrayerTimeMomentType.End
            )

            val subtimeOneTime = getCorrectSubTime(
                prayerTimeEntity.prayerTimeType,
                EPrayerTimeMomentType.SubTimeOne,
                prayerTimeEntity
            )

            val subtimeTwoTime = getCorrectSubTime(
                prayerTimeEntity.prayerTimeType,
                EPrayerTimeMomentType.SubTimeTwo,
                prayerTimeEntity
            )

            val subtimeThreeTime = getCorrectSubTime(
                prayerTimeEntity.prayerTimeType,
                EPrayerTimeMomentType.SubTimeThree,
                prayerTimeEntity
            )

            prayerTimeEntity.beginningTime = beginningTime
            prayerTimeEntity.endTime = endTime
            prayerTimeEntity.subtime1BeginningTime = beginningTime
            prayerTimeEntity.subtime1EndTime = subtimeOneTime
            prayerTimeEntity.subtime2BeginningTime = subtimeOneTime
            prayerTimeEntity.subtime2EndTime = subtimeTwoTime
            prayerTimeEntity.subtime3BeginningTime = subtimeTwoTime
            prayerTimeEntity.subtime3EndTime = subtimeThreeTime
        }
    }

    private fun getCorrectBeginningAndEndTime(
        prayerType: EPrayerTimeType,
        prayerTypeTimeType: EPrayerTimeMomentType
    ): LocalDateTime? {

        val prayerSettings = AppEnvironment.prayerSettingsByPrayerType[prayerType]

        if (prayerSettings != null
            && (prayerTypeTimeType === EPrayerTimeMomentType.Beginning || prayerTypeTimeType === EPrayerTimeMomentType.End)
        ) {
            var prayerBeginningEndSettings: PrayerTimeBeginningEndSettingsEntity? = null

            if (prayerTypeTimeType === EPrayerTimeMomentType.Beginning) {
                prayerBeginningEndSettings = prayerSettings.beginningSettings
            } else if (prayerTypeTimeType === EPrayerTimeMomentType.End) {
                prayerBeginningEndSettings = prayerSettings.endSettings
            }

            var correctTime: LocalDateTime? = null

            if (prayerBeginningEndSettings != null) {

                // TODO: Isha-Ende muss Fajr des  *Folgetages* sein!
                val prayerTimeWithType: AbstractMap.SimpleEntry<EPrayerTimeType, EPrayerTimeMomentType> =
                    AbstractMap.SimpleEntry<EPrayerTimeType, EPrayerTimeMomentType>(prayerType, prayerTypeTimeType)

                if (prayerBeginningEndSettings.api === ESupportedAPIs.Muwaqqit
                    && muwaqqitTimesHashMap[prayerTimeWithType] != null
                ) {
                    correctTime = muwaqqitTimesHashMap[prayerTimeWithType]!!.GetTimeByType(prayerType, prayerTypeTimeType)
                }
                else if (prayerBeginningEndSettings.api === ESupportedAPIs.Diyanet
                    && diyanetTimesHashMap[prayerTimeWithType] != null
                ) {
                    correctTime = diyanetTimesHashMap[prayerTimeWithType]!!.GetTimeByType(prayerType, prayerTypeTimeType)
                }
                else if (prayerBeginningEndSettings.api === ESupportedAPIs.AlAdhan
                    && alAdhanTimesHashMap[prayerTimeWithType] != null
                ) {
                    correctTime = alAdhanTimesHashMap[prayerTimeWithType]!!.GetTimeByType(prayerType, prayerTypeTimeType)
                }

                if (correctTime != null) {
                    val minuteAdjustment = prayerBeginningEndSettings.minuteAdjustment.toLong()

                    // minute adjustment
                    correctTime = correctTime.plusMinutes(minuteAdjustment)
                    return correctTime
                }
            }
        }
        return null
    }

    private fun getCorrectSubTime(
        prayerType: EPrayerTimeType,
        prayerTypeTimeType: EPrayerTimeMomentType,
        prayerTimeEntity: PrayerTimeEntity
    ): LocalDateTime? {

        val prayerSettings = AppEnvironment.prayerSettingsByPrayerType[prayerType]

        if (prayerSettings != null
            && prayerTypeTimeType != EPrayerTimeMomentType.Beginning
            && prayerTypeTimeType != EPrayerTimeMomentType.End) {

            var subTimeSettings: SubTimeSettingsEntity? =
                when (prayerTypeTimeType) {
                    EPrayerTimeMomentType.SubTimeOne,
                    EPrayerTimeMomentType.SubTimeTwo,
                    EPrayerTimeMomentType.SubTimeThree -> prayerSettings.subPrayer1Settings
                    else -> null
                }

            if (subTimeSettings != null) {

                val prayerTimeWithMomentType = AbstractMap.SimpleEntry<EPrayerTimeType, EPrayerTimeMomentType>(prayerType, prayerTypeTimeType)

                when (prayerType) {

                    EPrayerTimeType.Asr ->

                        if (prayerTypeTimeType === EPrayerTimeMomentType.SubTimeOne && subTimeSettings.isEnabled1) {
                            return muwaqqitTimesHashMap.get(prayerTimeWithMomentType)!!.mithlaynTime
                        }
                        else if (prayerTypeTimeType === EPrayerTimeMomentType.SubTimeTwo && subTimeSettings.isEnabled2) {
                            return muwaqqitTimesHashMap.get(prayerTimeWithMomentType)!!.asrKarahaTime
                        }

                    EPrayerTimeType.Maghrib ->

                        if (prayerTypeTimeType === EPrayerTimeMomentType.SubTimeOne && subTimeSettings.isEnabled1) {
                            return alAdhanTimesHashMap.get(prayerTimeWithMomentType)?.ishtibaqAnNujumTime
                        }

                    EPrayerTimeType.Isha -> {

                        if (prayerTimeEntity.Duration == 0L || PrayerTimeEntity.Maghrib.Duration == 0L) {
                            return null
                        }

                        val timeBetweenIshaBeginningAndMaghribEnd =
                            ChronoUnit.MILLIS.between(PrayerTimeEntity.Maghrib.endTime, prayerTimeEntity.beginningTime)

                        val nightDuration = prayerTimeEntity.Duration + PrayerTimeEntity.Maghrib.Duration + timeBetweenIshaBeginningAndMaghribEnd

                        if (prayerTypeTimeType !== EPrayerTimeMomentType.SubTimeThree && subTimeSettings.isEnabled1) {

                            val thirdOfNight = nightDuration / 3

                            if(prayerTypeTimeType == EPrayerTimeMomentType.SubTimeOne) {
                                return PrayerTimeEntity.Maghrib
                                    .beginningTime!!.plus(thirdOfNight, ChronoField.MILLI_OF_DAY.baseUnit)
                            }
                            else if(prayerTypeTimeType == EPrayerTimeMomentType.SubTimeTwo) {
                                return PrayerTimeEntity.Maghrib
                                    .beginningTime!!.plus(2 * thirdOfNight, ChronoField.MILLI_OF_DAY.baseUnit)
                            }

                        } else if (subTimeSettings.isEnabled2) {

                            val halfOfNight = nightDuration / 2

                            if(prayerTypeTimeType == EPrayerTimeMomentType.SubTimeThree) {
                                return PrayerTimeEntity.Maghrib
                                    .beginningTime!!.plus(halfOfNight, ChronoField.MILLI_OF_DAY.baseUnit)
                            }
                        }

                    }
                }
            }
        }
        return null
    }

    /**
     * Heutiges Datum, gespeicherten Standort, Zeiten der Gebete und grafische Darstellung.
     */
    private fun syncTimeInformationToUserInterface() {
        try {

            displayedDateTextLabel.text =
                DateTimeFormatter.ofPattern("dd.MM.yyyy").format(
                    LocalDateTime.now()
                )

            var cityName: String? = "-"

            if (AppEnvironment.PlaceEntity != null) {
                cityName = AppEnvironment.PlaceEntity!!.name
            }

            val autocompleteSupportFragment = googlePlaceSearchAutoCompleteFragment as AutocompleteSupportFragment

            val editText: AppCompatEditText =
                autocompleteSupportFragment.requireView().findViewById(R.id.places_autocomplete_search_input)

            editText.setText(cityName)

            val defaultDisplayText: String = this.resources.getString(R.string.no_time_display_text);

            for (prayerTimeEntity in PrayerTimeEntity.Prayers) {

                val beginningText: String = prayerTimeEntity.beginningTime?.format(timeFormat) ?: defaultDisplayText

                getByPrayerTypeAndTimeType(
                    prayerTimeEntity.prayerTimeType,
                    EPrayerTimeMomentType.Beginning
                )?.text = beginningText

                val endText: String = prayerTimeEntity.endTime?.format(timeFormat) ?: defaultDisplayText

                getByPrayerTypeAndTimeType(
                    prayerTimeEntity.prayerTimeType,
                    EPrayerTimeMomentType.End
                )?.text = endText

                // ##############################
                // ##############################

                // SubTimeOne
                val subtime1EndText: String = prayerTimeEntity.subtime1EndTime?.format(timeFormat) ?: defaultDisplayText

                getByPrayerTypeAndTimeType(
                    prayerTimeEntity.prayerTimeType,
                    EPrayerTimeMomentType.SubTimeOne
                )?.text = subtime1EndText

                // SubTimeTwo
                val subtime2EndText: String = prayerTimeEntity.subtime2EndTime?.format(timeFormat) ?: defaultDisplayText

                getByPrayerTypeAndTimeType(
                    prayerTimeEntity.prayerTimeType,
                    EPrayerTimeMomentType.SubTimeTwo
                )?.text = subtime2EndText

                // SubTimeThree
                val subtime3EndText: String = prayerTimeEntity.subtime3EndTime?.format(timeFormat) ?: defaultDisplayText

                getByPrayerTypeAndTimeType(
                    prayerTimeEntity.prayerTimeType,
                    EPrayerTimeMomentType.SubTimeThree
                )?.text = subtime3EndText
            }

            val currentLocalDateTime = LocalDateTime.now()
            prayerTimeGraphicView.displayPrayerEntity =
                getPrayerByTime(currentLocalDateTime)
            prayerTimeGraphicView.invalidate()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        var INTENT_EXTRA = "prayerTime"
        private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    }
}