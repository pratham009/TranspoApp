package com.track.parcel.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.track.parcel.BuildConfig;
import com.track.parcel.R;
import com.track.parcel.adapters.BookingAdapter;
import com.track.parcel.adapters.StatusAdapter;
import com.track.parcel.databinding.ActivityDriverHomeBinding;
import com.track.parcel.models.BookingModel;
import com.track.parcel.utilities.APIs;
import com.track.parcel.utilities.Constants;
import com.track.parcel.utilities.SessionApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import atirek.pothiwala.connection.Connector;
import atirek.pothiwala.utility.helper.IntentHelper;
import atirek.pothiwala.utility.helper.Loader;
import atirek.pothiwala.utility.helper.PhotoHelper;
import okhttp3.RequestBody;
import retrofit2.Call;

import atirek.pothiwala.utility.components.*;


public class DriverHomeActivity extends AppCompatActivity {

    ActivityDriverHomeBinding binding;
    BookingAdapter bookingAdapter;
    StatusAdapter statusAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnMenu.setOnClickListener(v -> {
            binding.drawerLayout.openDrawer(GravityCompat.START, true);
        });
        binding.btnProfile.setOnClickListener(view -> startActivity(new Intent(DriverHomeActivity.this, ProfileActivity.class)));
        binding.btnLogout.setOnClickListener(v -> {
            SessionApplication.toast(getString(R.string.loggedOutSuccessfully));
            SessionApplication.storage().clear();
            IntentHelper.restart(this);
        });

        statusAdapter = new StatusAdapter(DriverHomeActivity.this);
        binding.spinnerStatus.setAdapter(statusAdapter);
        binding.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getBookings();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
        updateUser();
        getBookings();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    void updateUser(){
        new PhotoHelper("https://picsum.photos/200")
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .load(binding.ivProfile);
        binding.tvName.setText(SessionApplication.storage().get("name"));
        binding.tvEmail.setText(SessionApplication.storage().get("email"));
    }

    void getBookings() {
        JSONObject params = new JSONObject();
        try {
            params.put("id", String.valueOf(SessionApplication.storage().get("id")));
            params.put("status", binding.spinnerStatus.getSelectedItem());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());

        Call<String> call = Connector.getClient(Constants.BASE_URL).create(APIs.class).driverGetBookings(requestBody);

        Connector connector = new Connector(this, BuildConfig.DEBUG);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(int statusCode, @Nullable String json, @NonNull String message) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray result = jsonObject.optJSONArray("result");
                    runOnUiThread(() -> bookingAdapter.setList(BookingModel.toList(result)));

                } catch (Exception e) {
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