package com.example.advancedprayertimes.Logic.Entities.API_Entities.Diyanet;

import com.google.gson.annotations.SerializedName;

public class DiyanetIlceEntity
{
    // region fields

    @SerializedName("IlceID")
    private String _ilceID = null;

    @SerializedName("IlceAdiEn")
    private String _ilceAdiEn = null;

    // endregion fields

    // region getter & setter

    public String getIlceID()
    {
        return _ilceID;
    }

    public void setIlceID(String ilceID)
    {
        _ilceID = ilceID;
    }

    public String getIlceAdiEn()
    {
        return _ilceAdiEn;
    }

    public void setIlceAdiEn(String ilceAdiEn)
    {
        _ilceAdiEn = ilceAdiEn;
    }

    // endregion getter & setter
}
