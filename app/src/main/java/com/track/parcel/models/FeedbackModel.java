package com.track.parcel.models;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class FeedbackModel {

    @SerializedName("fid")
    public String id;

    @SerializedName("bid")
    public String bid;

    @SerializedName("uid")
    public String uid;

    @SerializedName("frating")
    public String rating;

    @SerializedName("fcomments")
    public String comments;

    @SerializedName("fdate")
    public String date;

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static FeedbackModel toModel(JSONObject json) {
        return new Gson().fromJson(json.toString(), FeedbackModel.class);
    }

    public static List<FeedbackModel> toList(@Nullable JSONArray jsonArray) {
        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }
        Type type = new TypeToken<List<FeedbackModel>>() {
        }.getType();
        return new Gson().fromJson(jsonArray.toString(), type);
    }
}
