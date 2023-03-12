package com.track.parcel.models;

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

public class BookingModel {

    @SerializedName("bid")
    public String id;

    @SerializedName("source")
    public String source;

    @SerializedName("source_lat")
    public String sourceLatitude;

    @SerializedName("source_long")
    public String sourceLongitude;

    @SerializedName("destination")
    public String destination;

    @SerializedName("desti_lat")
    public String destinationLatitude;

    @SerializedName("desti_long")
    public String destinationLongitude;

    @SerializedName("b_status")
    public String bookingStatus;

    @SerializedName("bdate")
    public String date;

    @SerializedName("uid")
    public String customerId;

    @SerializedName("did")
    public String driverId;

    @SerializedName("amount")
    public String amount;

    @SerializedName("kms")
    public String kms;

    @SerializedName(("bdesc"))
    public String description;

    public LatLng getSource() {
        return new LatLng(Double.parseDouble(sourceLatitude), Double.parseDouble(sourceLongitude));
    }

    public LatLng getDestination() {
        return new LatLng(Double.parseDouble(destinationLatitude), Double.parseDouble(destinationLongitude));
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static BookingModel toModel(JSONObject json) {
        return new Gson().fromJson(json.toString(), BookingModel.class);
    }

    public static List<BookingModel> toList(@Nullable JSONArray jsonArray) {
        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }
        Type type = new TypeToken<List<BookingModel>>() {
        }.getType();
        return new Gson().fromJson(jsonArray.toString(), type);
    }
}
