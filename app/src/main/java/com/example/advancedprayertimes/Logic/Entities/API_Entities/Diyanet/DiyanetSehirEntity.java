package com.example.advancedprayertimes.Logic.Entities.API_Entities.Diyanet;

import com.google.gson.annotations.SerializedName;

public class DiyanetSehirEntity
{
    // region fields

    @SerializedName("SehirID")
    private String _sehirID = null;

    @SerializedName("SehirAdiEn")
    private String _sehirAdiEn = null;

    // endregion fields

    // region getter & setter

    public String getSehirID()
    {
        return _sehirID;
    }

    public void setSehirID(String sehirID)
    {
        _sehirID = sehirID;
    }

    public String getSehirAdiEn()
    {
        return _sehirAdiEn;
    }

    public void setSehirAdiEn(String sehirAdiEn)
    {
        _sehirAdiEn = sehirAdiEn;
    }

    // endregion getter & setter
}
