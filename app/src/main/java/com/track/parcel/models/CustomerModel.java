package com.track.parcel.models;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.util.Locale;

public class CustomerModel {

    @SerializedName("uid")
    public String id;

    @SerializedName("uname")
    public String name;

    @SerializedName("ucontact")
    public String contact;

    @SerializedName("uemail")
    public String email;

    @SerializedName("ucity")
    public String city;

    @SerializedName("ulatitude")
    public String latitude;

    @SerializedName("ulongitude")
    public String longitude;

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public Location getLocation() {
        Location location = new Location(String.format(Locale.getDefault(), "customer_%s", id));
        location.setLatitude(Double.parseDouble(latitude));
        location.setLongitude(Double.parseDouble(longitude));
        return location;
    }

    public LatLng getLatLng() {
        return new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
    }

    public static CustomerModel toModel(JSONObject json) {
        return new Gson().fromJson(json.toString(), CustomerModel.class);
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("name", name);
            jsonObject.put("contact", contact);
            jsonObject.put("email", email);
            jsonObject.put("city", city);
            jsonObject.put("type", "customer");
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
