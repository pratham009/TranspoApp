package com.track.parcel.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.track.parcel.BuildConfig;
import com.track.parcel.databinding.ActivityForgotPasswordBinding;
import com.track.parcel.utilities.APIs;
import com.track.parcel.utilities.Constants;
import com.track.parcel.utilities.SessionApplication;

import org.json.JSONException;
import org.json.JSONObject;

import atirek.pothiwala.connection.Connector;
import atirek.pothiwala.utility.helper.Loader;
import atirek.pothiwala.utility.helper.ValidationHelper;
import okhttp3.RequestBody;
import retrofit2.Call;


public class ForgotPasswordActivity extends AppCompatActivity {

    ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSubmit.setOnClickListener(view -> {
            if (!ValidationHelper.isValidEmail(binding.etEmail)) {
                return;
            }
            SessionApplication.toast("Your password reset instruction details have been sent to your email address, please check.");
        });
    }

    void forgotPassword() {
        boolean isCustomer = binding.rbCustomer.isChecked();

        if (!ValidationHelper.isValidEmail(binding.etEmail)) {
            return;
        }
        String email = binding.etEmail.getText().toString();

        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());

        Call<String> call;
        if (!isCustomer) {
            call = Connector.getClient(Constants.BASE_URL).create(APIs.class).driverForgotPassword(requestBody);
        } else {
            call = Connector.getClient(Constants.BASE_URL).create(APIs.class).userForgotPassword(requestBody);
        }
        Connector connector = new Connector(this, BuildConfig.DEBUG);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(int statusCode, @Nullable String json, @NonNull String message) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    int response = jsonObject.getInt("response");
                    if (response == 1) {
                        binding.etEmail.setText("");
                    }
                    SessionApplication.toast(jsonObject.optString("message"));
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
        connector.Request("forgot_password", call);
    }
}