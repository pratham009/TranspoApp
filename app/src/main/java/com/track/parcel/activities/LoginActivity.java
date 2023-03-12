package com.track.parcel.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.track.parcel.BuildConfig;
import com.track.parcel.databinding.ActivityLoginBinding;
import com.track.parcel.models.CustomerModel;
import com.track.parcel.models.DriverModel;
import com.track.parcel.utilities.APIs;
import com.track.parcel.utilities.Constants;
import com.track.parcel.utilities.SessionApplication;

import org.json.JSONException;
import org.json.JSONObject;

import atirek.pothiwala.connection.Connector;
import atirek.pothiwala.utility.helper.Loader;
import atirek.pothiwala.utility.helper.PermissionHelper;
import atirek.pothiwala.utility.helper.ValidationHelper;
import okhttp3.RequestBody;
import retrofit2.Call;


public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        updateUI();

        binding.btnSignUp.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
        binding.btnForgotPassword.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
        binding.btnLogin.setOnClickListener(v -> loginUser());

    }

    void updateUI() {
        binding.etEmail.setText("sahilmansuri@gmail.com");
        binding.etPassword.setText("12345678");
    }

    void loginUser() {
        if(!requestLocation()){
            return;
        }
        boolean isCustomer = binding.rbCustomer.isChecked();

        if (!ValidationHelper.isValidEmail(binding.etEmail)) {
            return;
        }
        String email = binding.etEmail.getText().toString();

        if (!ValidationHelper.isValidString(binding.etPassword, 3)) {
            return;
        }
        String password = binding.etPassword.getText().toString();

        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
            params.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());

        Call<String> call;
        if (!isCustomer) {
            call = Connector.getClient(Constants.BASE_URL).create(APIs.class).driverLogin(requestBody);
        } else {
            call = Connector.getClient(Constants.BASE_URL).create(APIs.class).userLogin(requestBody);
        }

        Connector connector = new Connector(this, BuildConfig.DEBUG);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(int statusCode, @Nullable String json, @NonNull String message) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    int response = jsonObject.getInt("response");
                    if (response == 1) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        if (isCustomer) {
                            SessionApplication.storage().set(CustomerModel.toModel(data).toJson());
                            startActivity(new Intent(LoginActivity.this, CustomerHomeActivity.class));
                        } else {
                            SessionApplication.storage().set(DriverModel.toModel(data).toJson());
                            startActivity(new Intent(LoginActivity.this, DriverHomeActivity.class));
                        }
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
        connector.Request("login", call);
    }

    boolean requestLocation(){
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (!PermissionHelper.checkPermissions(this, permissions)) {
            PermissionHelper.requestPermissions(this, permissions, 0);
            return false;
        }
        return true;
    }
}