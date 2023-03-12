package com.track.parcel.models;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

public class DriverModel {

    @SerializedName("did")
    public String id;

    @SerializedName("dname")
    public String name;

    @SerializedName("dcontact")
    public String contact;

    @SerializedName("demail")
    public String email;

    @SerializedName("daddress")
    public String address;

    @SerializedName("dcity")
    public String city;

    @SerializedName("dlicense")
    public String license;

    @SerializedName("dphoto")
    public String photo;

    @SerializedName("dlatitude")
    public String latitude;

    @SerializedName("dlongitude")
    public String longitude;

    @SerializedName("vreg")
    public String numberPlate;

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public Location getLocation() {
        Location location = new Location(String.format(Locale.getDefault(), "driver_%s", id));
        location.setLatitude(Double.parseDouble(latitude == null ? "0" : latitude));
        location.setLongitude(Double.parseDouble(longitude == null ? "0" : longitude));
        return location;
    }

    public LatLng getLatLng() {
        double latitude = Double.parseDouble(this.latitude == null ? "0" : this.latitude);
        double longitude = Double.parseDouble(this.longitude == null ? "0" : this.longitude);
        return new LatLng(latitude, longitude);
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("name", name);
            jsonObject.put("contact", contact);
            jsonObject.put("email", email);
            jsonObject.put("address", address);
            jsonObject.put("city", city);
            jsonObject.put("license", license);
            jsonObject.put("photo", photo);
            jsonObject.put("type", "driver");
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
            jsonObject.put("numberPlate", numberPlate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static DriverModel toModel(JSONObject json) {
        return new Gson().fromJson(json.toString(), DriverModel.class);
    }

    public static List<DriverModel> toList(@Nullable JSONArray jsonArray) {
        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }
        Type type = new TypeToken<List<DriverModel>>() {
        }.getType();
        return new Gson().fromJson(jsonArray.toString(), type);
    }

}
