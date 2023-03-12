package com.track.parcel.activities;

import static atirek.pothiwala.utility.helper.ValidationHelper.ErrorText.cannotBeEmpty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.track.parcel.BuildConfig;
import com.track.parcel.R;
import com.track.parcel.databinding.ActivityBookBinding;
import com.track.parcel.models.DriverModel;
import com.track.parcel.utilities.APIs;
import com.track.parcel.utilities.SessionApplication;
import com.google.android.gms.maps.model.LatLng;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.Constants;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import atirek.pothiwala.connection.Connector;
import atirek.pothiwala.utility.helper.IntentHelper;
import atirek.pothiwala.utility.helper.Loader;
import atirek.pothiwala.utility.helper.PermissionHelper;
import atirek.pothiwala.utility.helper.PhotoHelper;
import atirek.pothiwala.utility.helper.TextHelper;
import atirek.pothiwala.utility.helper.Tools;
import atirek.pothiwala.utility.helper.ValidationHelper;
import okhttp3.RequestBody;
import retrofit2.Call;

public class BookActivity extends AppCompatActivity {

    private static final int REQUEST_SOURCE_ADDRESS = 1000;
    private static final int REQUEST_DESTINATION_ADDRESS = 1001;

    private static final int DRIVER_DISTANCE_IN_METRES = 10 * 1000;
    private static final int DRIVER_DURATION_IN_SECONDS = 30 * 1000;
    private Call<String> callDrivers;

    private ActivityBookBinding binding;
    private DriverModel driver;
    private LatLng location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        try {
            location = intent.getParcelableExtra("location");

            updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            binding.btnCall.setOnClickListener(v -> callDriver());
            binding.etSource.setOnClickListener(v -> pickPlace(REQUEST_SOURCE_ADDRESS));
            binding.etDestination.setOnClickListener(v -> pickPlace(REQUEST_DESTINATION_ADDRESS));
            binding.btnBook.setOnClickListener(v -> createBooking());
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        checkDrivers();
    }

    private void updateUI() {
        if (driver != null) {
            binding.layoutDriver.setVisibility(View.VISIBLE);

            new PhotoHelper("https://picsum.photos/200").placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile).load(binding.ivDriver);
            binding.tvDriver.setText(TextHelper.capitalizeWord(driver.name));
            binding.tvNumberPlate.setText(driver.numberPlate);
        } else {
            binding.layoutDriver.setVisibility(View.GONE);
        }

        double[] summary = getSummary();
        double distance = summary[0];
        double vehicleCharges = summary[1];
        double serviceCharges = summary[2];
        double totalCharges = summary[3];

        binding.tvDistance.setText(String.format(Locale.getDefault(), "%.1f kms away", distance));
        binding.tvVehicleCharge.setText(String.format(Locale.getDefault(), "₹%.1f", vehicleCharges));
        binding.tvServiceCharges.setText(String.format(Locale.getDefault(), "₹%.1f", serviceCharges));
        binding.tvTotal.setText(String.format(Locale.getDefault(), "₹%.1f", totalCharges));
    }

    private double[] getSummary() {
        Object sourceObject = binding.etSource.getTag();
        Object destinationObject = binding.etDestination.getTag();

        double distance = 0;
        if (sourceObject != null && destinationObject != null) {
            Address source = (Address) sourceObject;
            Address destination = (Address) destinationObject;

            Location sourceLocation = new Location("source");
            sourceLocation.setLatitude(source.getLatitude());
            sourceLocation.setLongitude(source.getLongitude());

            Location destinationLocation = new Location("destination");
            destinationLocation.setLatitude(destination.getLatitude());
            destinationLocation.setLongitude(destination.getLongitude());
            distance = sourceLocation.distanceTo(destinationLocation) / 1000;
        }

        double vehicleCharges = 25.0;
        double serviceCharges = com.track.parcel.utilities.Constants.SERVICE_CHARGES;
        double totalCharges = distance * vehicleCharges + serviceCharges;

        return new double[]{distance, vehicleCharges, serviceCharges, totalCharges};
    }

    private void pickPlace(int request) {
        Intent intent = new PlacePicker.IntentBuilder().setLatLong(location.latitude, location.longitude)  // Initial Latitude and Longitude the Map will load into
                .showLatLong(true)  // Show Coordinates in the Activity
                .setMapZoom(18.0f)  // Map Zoom Level. Default: 14.0
                .setAddressRequired(true) // Set If return only Coordinates if cannot fetch Address for the coordinates. Default: True
                .hideMarkerShadow(true) // Hides the shadow under the map marker. Default: False
                .setMarkerDrawable(R.drawable.ic_location) // Change the default Marker Image
                .setFabColor(R.color.black).setPrimaryTextColor(R.color.black) // Change text color of Shortened Address
                .setSecondaryTextColor(R.color.white) // Change text color of full Address
                .setBottomViewColor(R.color.black) // Change Address View Background Color (Default: White)
                .setMapType(MapType.NORMAL).setPlaceSearchBar(false, getString(R.string.google_maps_key)) //Activate GooglePlace Search Bar. Default is false/not activated. SearchBar is a chargeable feature by Google
                .onlyCoordinates(false)  //Get only Coordinates from Place Picker
                .hideLocationButton(false)   //Hide Location Button (Default: false)
                .disableMarkerAnimation(true)   //Disable Marker Animation (Default: false)
                .build(this);
        startActivityForResult(intent, request);
    }

    private void callDriver() {
        String[] permissions = new String[]{Manifest.permission.CALL_PHONE};
        if (!PermissionHelper.checkPermissions(BookActivity.this, permissions)) {
            PermissionHelper.requestPermissions(BookActivity.this, permissions, 0);
            return;
        }
        IntentHelper.phoneCall(BookActivity.this, driver.contact);
    }

    private void createBooking() {
        if (!ValidationHelper.isNonEmpty(binding.etDescription)) {
            return;
        }
        if (!isNonEmpty(binding.etSource)) {
            return;
        }
        if (!isNonEmpty(binding.etDestination)) {
            return;
        }
        if(driver == null){
            SessionApplication.toast("No delivery boy within " + (DRIVER_DISTANCE_IN_METRES / 1000) +  " kms radius!");
            return;
        }

        Object sourceObject = binding.etSource.getTag();
        Object destinationObject = binding.etDestination.getTag();
        if (sourceObject == null || destinationObject == null) {
            return;
        }
        Address source = (Address) sourceObject;
        Address destination = (Address) destinationObject;

        double[] summary = getSummary();
        double distance = summary[0];
        double totalCharges = summary[3];

        if (distance < 1) {
            SessionApplication.toast("Minimum distance must be above 1 km.");
            return;
        }

        JSONObject params = new JSONObject();
        try {
            params.put("s_address", source.getAddressLine(0));
            params.put("d_address", destination.getAddressLine(0));
            params.put("uid", SessionApplication.storage().get("id"));
            params.put("did", driver.id);
            params.put("status", "pending");
            params.put("amount", String.format(Locale.getDefault(), "%.1f", totalCharges));
            params.put("kms", String.format(Locale.getDefault(), "%.1f", distance));
            params.put("s_lat", String.valueOf(source.getLatitude()));
            params.put("s_long", String.valueOf(source.getLongitude()));
            params.put("d_lat", String.valueOf(destination.getLatitude()));
            params.put("d_long", String.valueOf(destination.getLongitude()));
            params.put("details", binding.etDescription.getText().toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());
        Call<String> call = Connector.getClient(com.track.parcel.utilities.Constants.BASE_URL).create(APIs.class).bookingCreate(requestBody);

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

    private void getDrivers() {
        handler.removeCallbacks(runnable);

        Connector connector = new Connector(this, BuildConfig.DEBUG);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(int statusCode, @Nullable String json, @NonNull String message) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    Address address = (Address) binding.etSource.getTag();
                    List<DriverModel> list = DriverModel.toList(jsonObject.optJSONArray("result"));

                    Location sourceLocation = new Location("source");
                    sourceLocation.setLongitude(address.getLongitude());
                    sourceLocation.setLatitude(address.getLatitude());

                    final double[] minimumDistance = {DRIVER_DISTANCE_IN_METRES};
                    list.forEach(model -> {
                        double distanceInMetres = sourceLocation.distanceTo(model.getLocation());
                        Log.d("drivers>>", "Name: " + model.name + " and Distance: " + distanceInMetres);

                        if (distanceInMetres <= minimumDistance[0]) {
                            minimumDistance[0] = distanceInMetres;
                            driver = model;
                        }
                    });
                    updateUI();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    checkDrivers();
                }
            }

            @Override
            public void onFailure(boolean isNetworkIssue, @NonNull String errorMessage) {
                SessionApplication.toast(errorMessage);
                checkDrivers();
            }
        });

        connector.cancelCall(callDrivers);

        callDrivers = Connector.getClient(com.track.parcel.utilities.Constants.BASE_URL).create(APIs.class).driverGetAll();
        connector.Request("get_drivers", callDrivers);
    }

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (binding.etSource.getTag() != null) {
                getDrivers();
            } else {
                checkDrivers();
            }
        }
    };

    private void checkDrivers() {
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, DRIVER_DURATION_IN_SECONDS);
    }

    public static boolean isNonEmpty(TextView textView) {
        String string = textView.getText().toString().trim();
        if (string.isEmpty()) {
            textView.setError(cannotBeEmpty);
            textView.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            AddressData addressData = data.getParcelableExtra(Constants.ADDRESS_INTENT);
            Log.d("address>>", addressData.toString());
            List<Address> addressList = addressData.getAddressList();
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                Log.d("address>>", address.getAddressLine(0));
                runOnUiThread(() -> {
                    if (requestCode == REQUEST_SOURCE_ADDRESS) {
                        dialogAddress(getString(R.string.source), binding.etSource, address);
                    } else {
                        dialogAddress(getString(R.string.destination), binding.etDestination, address);
                    }
                    updateUI();
                });
            }
        }
    }

    private void dialogAddress(String title, TextView editText, Address address) {

        int dp15 = (int) Tools.dpToPixels(this, 15);

        EditText etAddress = new EditText(this);
        etAddress.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        etAddress.setPadding(dp15, dp15, dp15, dp15);
        etAddress.setBackgroundResource(R.drawable.bg_black_60a);
        etAddress.setTextColor(Tools.getColor(this, R.color.white));
        etAddress.setSingleLine(false);
        etAddress.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        etAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etAddress.setLines(5);
        etAddress.setMaxLines(10);
        etAddress.setVerticalScrollBarEnabled(true);
        etAddress.setMovementMethod(ScrollingMovementMethod.getInstance());
        etAddress.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);

        String newAddress = address.getAddressLine(0);
        String existingAddress = editText.getText().toString().trim();
        etAddress.setText(existingAddress.isEmpty() ? newAddress : existingAddress);

        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp15, 0, dp15, 0);
        layout.addView(etAddress);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage("Enter Your Address");
        alert.setView(layout);

        alert.setPositiveButton("Confirm", (dialog, whichButton) -> {
            editText.setText(etAddress.getText().toString());
            editText.setTag(address);
            if (editText == binding.etSource) {
                getDrivers();
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }
}