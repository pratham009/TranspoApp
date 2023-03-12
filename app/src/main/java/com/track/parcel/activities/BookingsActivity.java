package com.track.parcel.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.track.parcel.BuildConfig;
import com.track.parcel.adapters.BookingAdapter;
import com.track.parcel.databinding.ActivityBookingsBinding;
import com.track.parcel.models.BookingModel;
import com.track.parcel.utilities.APIs;
import com.track.parcel.utilities.Constants;
import com.track.parcel.utilities.SessionApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import atirek.pothiwala.connection.Connector;
import atirek.pothiwala.utility.components.SpaceDecorate;
import atirek.pothiwala.utility.helper.Loader;
import okhttp3.RequestBody;
import retrofit2.Call;


public class BookingsActivity extends AppCompatActivity {

    ActivityBookingsBinding binding;
    BookingAdapter bookingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookingAdapter = new BookingAdapter(this);
        bookingAdapter.setViewEmpty(binding.tvEmpty);
        binding.recyclerView.setAdapter(bookingAdapter);

        SpaceDecorate spaceDecorate = new SpaceDecorate(this);
        spaceDecorate.setHorizontalMargin(com.intuit.sdp.R.dimen._15sdp);
        spaceDecorate.setVerticalMargin(com.intuit.sdp.R.dimen._15sdp);
        spaceDecorate.setHorizontalSpace(com.intuit.sdp.R.dimen._15sdp);
        spaceDecorate.setVerticalSpace(com.intuit.sdp.R.dimen._15sdp);
        binding.recyclerView.addItemDecoration(spaceDecorate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBookings();
    }

    private void getBookings() {
        JSONObject params = new JSONObject();
        try {
            params.put("id", SessionApplication.storage().get("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());

        Call<String> call = Connector.getClient(Constants.BASE_URL).create(APIs.class).userGetBookings(requestBody);

        Connector connector = new Connector(this, BuildConfig.DEBUG);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(int statusCode, @Nullable String json, @NonNull String message) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray result = jsonObject.optJSONArray("result");

                    List<BookingModel> list = BookingModel.toList(result);
                    Collections.reverse(list);
                    runOnUiThread(() -> bookingAdapter.setList(list));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(boolean isNetworkIssue, @NonNull String errorMessage) {
                SessionApplication.toast(errorMessage);
            }
        });

        Loader loader = new Loader(this);
        loader.setCancelListener(dialog -> connector.cancelCall(call));

        connector.setLoaderDialog(loader.getDialog());
        connector.Request("get_bookings", call);
    }
}