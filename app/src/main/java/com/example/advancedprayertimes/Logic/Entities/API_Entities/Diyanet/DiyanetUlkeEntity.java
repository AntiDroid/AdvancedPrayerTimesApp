package com.example.advancedprayertimes.Logic.Entities.API_Entities.Diyanet;

import com.google.gson.annotations.SerializedName;

public class DiyanetUlkeEntity
{
    // region fields

    @SerializedName("UlkeID")
    private String _ulkeID = null;

    @SerializedName("UlkeAdiEn")
    private String _ulkeAdiEn = null;

    // endregion fields

    // region getter & setter

    public String getUlkeID()
    {
        return _ulkeID;
    }

    public void setUlkeID(String ulkeID)
    {
        _ulkeID = ulkeID;
    }

    public String getUlkeAdiEn()
    {
        return _ulkeAdiEn;
    }

    public void setUlkeAdiEn(String ulkeAdiEn)
    {
        _ulkeAdiEn = ulkeAdiEn;
    }

    // endregion getter & setter
}
