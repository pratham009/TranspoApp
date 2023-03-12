package com.track.parcel.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.track.parcel.BuildConfig;
import com.track.parcel.R;
import com.track.parcel.databinding.ActivityProfileBinding;
import com.track.parcel.models.CustomerModel;
import com.track.parcel.models.DriverModel;
import com.track.parcel.utilities.APIs;
import com.track.parcel.utilities.Constants;
import com.track.parcel.utilities.SessionApplication;

import org.json.JSONException;
import org.json.JSONObject;

import atirek.pothiwala.connection.Connector;
import atirek.pothiwala.utility.helper.IntentHelper;
import atirek.pothiwala.utility.helper.Loader;
import atirek.pothiwala.utility.helper.PhotoHelper;
import atirek.pothiwala.utility.helper.ValidationHelper;
import okhttp3.RequestBody;
import retrofit2.Call;


public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogout.setOnClickListener(v -> {
            SessionApplication.toast(getString(R.string.loggedOutSuccessfully));
            SessionApplication.storage().clear();
            IntentHelper.restart(ProfileActivity.this);
        });
        binding.btnUpdate.setOnClickListener(v -> setProfile());

        updateUI();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getProfile();
    }

    boolean isCustomer() {
        return SessionApplication.isCustomer();
    }

    void updateUI() {
        new PhotoHelper("https://picsum.photos/200")
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .load(binding.ivProfile);
        binding.etName.setText(SessionApplication.storage().get("name"));
        binding.etEmail.setText(SessionApplication.storage().get("email"));
        binding.etMobile.setText(SessionApplication.storage().get("contact"));
        binding.etAddress.setText(SessionApplication.storage().get("address"));
        binding.etCity.setText(SessionApplication.storage().get("city"));

        if (isCustomer()) {
            binding.etAddress.setVisibility(View.GONE);
        } else {
            binding.etAddress.setVisibility(View.VISIBLE);
        }
    }

    void getProfile() {
        boolean isCustomer = isCustomer();

        JSONObject params = new JSONObject();
        try {
            params.put("id", SessionApplication.storage().get("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());

        Call<String> call;
        if (!isCustomer) {
            call = Connector.getClient(Constants.BASE_URL).create(APIs.class).driverGetProfile(requestBody);
        } else {
            call = Connector.getClient(Constants.BASE_URL).create(APIs.class).userGetProfile(requestBody);
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
                        } else {
                            SessionApplication.storage().set(DriverModel.toModel(data).toJson());
                        }
                        updateUI();
                    }
                    SessionApplication.toast(jsonObject.optString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    SessionApplication.toast(e.getMessage());
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
        connector.Request("get_profile", call);
    }

    void setProfile() {
        boolean isCustomer = isCustomer();

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
            if (!ValidationHelper.isNonEmpty(binding.etAddress)) {
                return;
            }
        }
        if (!ValidationHelper.isNonEmpty(binding.etCity)) {
            return;
        }

        String name = binding.etName.getText().toString();
        String email = binding.etEmail.getText().toString();
        String mobile = binding.etMobile.getText().toString();
        String address = binding.etAddress.getText().toString();
        String city = binding.etCity.getText().toString();

        JSONObject params = new JSONObject();
        try {
            params.put("id", SessionApplication.storage().get("id"));
            params.put("name", name);
            params.put("email", email);
            params.put("contact", mobile);
            params.put("address", address);
            params.put("city", city);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());

        Call<String> call;
        if (!isCustomer) {
            call = Connector.getClient(Constants.BASE_URL).create(APIs.class).driverSetProfile(requestBody);
        } else {
            call = Connector.getClient(Constants.BASE_URL).create(APIs.class).userSetProfile(requestBody);
        }
        Connector connector = new Connector(this, BuildConfig.DEBUG);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(int statusCode, @Nullable String json, @NonNull String message) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    int response = jsonObject.getInt("response");
                    if (response == 1) {
                        getProfile();
                    }
                    SessionApplication.toast(jsonObject.optString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    SessionApplication.toast(e.getMessage());
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
        connector.Request("set_profile", call);
    }
}