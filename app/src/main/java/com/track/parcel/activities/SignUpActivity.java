package com.track.parcel.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.track.parcel.BuildConfig;
import com.track.parcel.databinding.ActivitySignUpBinding;
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

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        updateUI();

        binding.btnSignUp.setOnClickListener(v -> signUpUser());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.rgUserType.setOnCheckedChangeListener((group, checkedId) -> updateUI());
    }

    void updateUI() {
        boolean isCustomer = binding.rbCustomer.isChecked();
        binding.etLicense.setVisibility(isCustomer ? View.GONE : View.VISIBLE);
        binding.etAddress.setVisibility(isCustomer ? View.GONE : View.VISIBLE);
    }

    void signUpUser() {
        if(!requestLocation()){
            return;
        }
        boolean isCustomer = binding.rbCustomer.isChecked();

        if (!ValidationHelper.isNonEmpty(binding.etName)) {
            return;
        }
        if (!ValidationHelper.isValidEmail(binding.etEmail)) {
            return;
        }
        if (!ValidationHelper.isValidPhoneNumber(binding.etMobile)) {
            return;
        }

        if (!isCustomer) {
            if (!ValidationHelper.isNonEmpty(binding.etLicense)) {
                return;
            }
            if (!ValidationHelper.isNonEmpty(binding.etAddress)) {
                return;
            }
        }

        if (!ValidationHelper.isNonEmpty(binding.etCity)) {
            return;
        }
        if (!ValidationHelper.isValidString(binding.etPassword, 3)) {
            return;
        }
        if (!ValidationHelper.isValidString(binding.etConfirmPassword, 3)) {
            return;
        }

        String name = binding.etName.getText().toString();
        String email = binding.etEmail.getText().toString();
        String mobile = binding.etMobile.getText().toString();
        String license = binding.etLicense.getText().toString();
        String address = binding.etAddress.getText().toString();
        String city = binding.etCity.getText().toString();
        String password = binding.etPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        if (!password.equalsIgnoreCase(confirmPassword)) {
            binding.etConfirmPassword.setError("Password Mismatch!");
            return;
        }

        JSONObject params = new JSONObject();
        try {
            params.put("name", name);
            params.put("email", email);
            params.put("contact", mobile);
            params.put("license", license);
            params.put("address", address);
            params.put("city", city);
            params.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());

        Call<String> call;
        if (!isCustomer) {
            call = Connector.getClient(Constants.BASE_URL).create(APIs.class).driverRegistration(requestBody);
        } else {
            call = Connector.getClient(Constants.BASE_URL).create(APIs.class).userRegistration(requestBody);
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
                            startActivity(new Intent(SignUpActivity.this, CustomerHomeActivity.class));
                        } else {
                            SessionApplication.storage().set(DriverModel.toModel(data).toJson());
                            startActivity(new Intent(SignUpActivity.this, DriverHomeActivity.class));
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
        connector.Request("register", call);

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