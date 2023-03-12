package com.track.parcel.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.track.parcel.BuildConfig;
import com.track.parcel.databinding.ActivityFeedbackBinding;
import com.track.parcel.models.BookingModel;
import com.track.parcel.utilities.APIs;
import com.track.parcel.utilities.SessionApplication;

import org.json.JSONObject;

import java.util.Locale;

import atirek.pothiwala.connection.Connector;
import atirek.pothiwala.utility.helper.Loader;
import atirek.pothiwala.utility.helper.ValidationHelper;
import okhttp3.RequestBody;
import retrofit2.Call;

public class FeedbackActivity extends AppCompatActivity {

    private ActivityFeedbackBinding binding;
    BookingModel booking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        try {
            booking = BookingModel.toModel(new JSONObject(intent.getStringExtra("booking")));
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        } finally {
            binding.btnSubmit.setOnClickListener(v -> createFeedback());
        }
    }

    private void createFeedback() {
        if (!ValidationHelper.isNonEmpty(binding.etFeedback)) {
            return;
        }
        String feedback = binding.etFeedback.getText().toString();

        JSONObject params = new JSONObject();
        try {
            params.put("bid", booking.id);
            params.put("uid", SessionApplication.storage().get("id"));
            params.put("rating", String.format(Locale.getDefault(), "%.1f", binding.ratingBar.getRating()));
            params.put("comments", feedback);

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());
        Call<String> call = Connector.getClient(com.track.parcel.utilities.Constants.BASE_URL).create(APIs.class).feedbackCreate(requestBody);

        Connector connector = new Connector(this, BuildConfig.DEBUG);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(int statusCode, @Nullable String json, @NonNull String message) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    int response = jsonObject.getInt("response");
                    if (response == 1) {
                        finish();
                    }
                    SessionApplication.toast(jsonObject.optString("message"));
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
        connector.Request("booking_create", call);

    }
}