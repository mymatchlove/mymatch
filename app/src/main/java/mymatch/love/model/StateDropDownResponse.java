package mymatch.love.model;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Momin Nasirali on 13/11/21.
 */
public class StateDropDownResponse {
    @SerializedName("country_name")
    private String countryName;

    @SerializedName("list")
    private List<KeyPairBoolData> list = null;

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public List<KeyPairBoolData> getList() {
        return list;
    }

    public void setList(List<KeyPairBoolData> list) {
        this.list = list;
    }
}
