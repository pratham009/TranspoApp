package com.track.parcel.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.track.parcel.BuildConfig;
import com.track.parcel.R;
import com.track.parcel.databinding.ActivityTrackBookingBinding;
import com.track.parcel.models.BookingModel;
import com.track.parcel.models.DriverModel;
import com.track.parcel.utilities.APIs;
import com.track.parcel.utilities.Constants;
import com.track.parcel.utilities.SessionApplication;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import atirek.pothiwala.connection.Connector;
import atirek.pothiwala.utility.helper.FusedLocationHelper;
import okhttp3.RequestBody;
import retrofit2.Call;


public class TrackBookingActivity extends AppCompatActivity implements OnMapReadyCallback, FusedLocationHelper.LocationListener {

    Handler handler;
    private static final int DEFAULT_ZOOM = 14;
    private static final int DEFAULT_TILT = 0;

    private BookingModel model;
    private GoogleMap googleMap;
    private FusedLocationHelper locationHelper;
    private Marker driverMarker, sourceMarker, destinationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTrackBookingBinding binding = ActivityTrackBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            String json = getIntent().getStringExtra("booking");
            model = BookingModel.toModel(new JSONObject(json));

        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

        handler = new Handler(getMainLooper());
        locationHelper = new FusedLocationHelper(this, "location", BuildConfig.DEBUG);
        locationHelper.setListener(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        this.googleMap.setMyLocationEnabled(true);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);
        this.googleMap.getUiSettings().setCompassEnabled(true);
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.getUiSettings().setZoomGesturesEnabled(true);

        this.createBookingMarker();
        this.handler.post(runnable);

        this.locationHelper.initializeLocationProviders();
        this.locationHelper.startLocationUpdates();

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            onBackPressed();
        }
    }

    void adjustZoomLevel(LatLng source) {
        CameraPosition cameraPosition = CameraPosition.builder().target(source).tilt(DEFAULT_TILT).zoom(DEFAULT_ZOOM).bearing(0).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    protected void onDestroy() {
        locationHelper.stopLocationUpdates();
        endCalling();
        super.onDestroy();
    }

    @Override
    public void onLocationReceived(@NonNull Location location) {
        locationHelper.stopLocationUpdates();
        adjustZoomLevel(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onLocationAvailability(boolean isAvailable) {

    }

    private void createBookingMarker() {
        if (model.bookingStatus.equalsIgnoreCase("accepted")) {
            if (sourceMarker == null) {
                sourceMarker = this.googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location))
                        .position(model.getSource())
                        .title("Source")
                        .snippet(model.source)
                        .draggable(false));
            }
        } else {
            if (sourceMarker != null) {
                sourceMarker.remove();
            }
        }

        if (model.bookingStatus.equalsIgnoreCase("started")) {
            if (destinationMarker == null) {
                destinationMarker = this.googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location))
                        .position(model.getDestination())
                        .title("Destination")
                        .snippet(model.destination)
                        .draggable(false));
            }
        } else {
            if (destinationMarker != null) {
                destinationMarker.remove();
            }
        }

        if (model.bookingStatus.equalsIgnoreCase("started")) {
            SessionApplication.toast("Delivery Started");
        } else if (model.bookingStatus.equalsIgnoreCase("delivered")) {
            endCalling();
            updateDialog("Delivery Completed", (dialog, which) -> finish());
        }

    }

    AlertDialog dialog;

    private void updateDialog(String message, DialogInterface.OnClickListener listener) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(message);
        builder.setPositiveButton("Ok", listener);
        dialog = builder.show();
    }

    private void setDriverMarker(@NonNull DriverModel model) {
        if (driverMarker == null) {
            driverMarker = this.googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_delivery_bike))
                    .position(model.getLatLng())
                    .title(model.name)
                    .draggable(false));
            driverMarker.setTag(model);
            driverMarker.showInfoWindow();

        } else {
            Location oldLocation = new Location("old");
            oldLocation.setLatitude(driverMarker.getPosition().latitude);
            oldLocation.setLongitude(driverMarker.getPosition().longitude);

            Location newLocation = model.getLocation();
            newLocation.setBearing(oldLocation.bearingTo(newLocation));
            Constants.animateMarker(newLocation, driverMarker);
        }
    }


    Call<String> driverCall;
    Call<String> bookingCall;

    private void getDriver() {
        JSONObject params = new JSONObject();
        try {
            params.put("id", model.driverId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());

        cancelGetDriver();
        driverCall = Connector.getClient(Constants.BASE_URL).create(APIs.class).driverGetProfile(requestBody);

        Connector connector = new Connector(this, BuildConfig.DEBUG);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(int statusCode, @Nullable String json, @NonNull String message) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    int response = jsonObject.getInt("response");
                    if (response == 1) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        setDriverMarker(DriverModel.toModel(data));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    startCalling();
                }
            }

            @Override
            public void onFailure(boolean isNetworkIssue, @NonNull String errorMessage) {
                SessionApplication.toast(errorMessage);
            }
        });
        connector.Request("get_driver", driverCall);
    }

    private void cancelGetDriver() {
        handler.removeCallbacks(runnable);
        if (driverCall != null && !driverCall.isCanceled()) {
            driverCall.cancel();
        }
    }


    private void getBooking() {
        JSONObject params = new JSONObject();
        try {
            params.put("id", model.id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());

        cancelGetBooking();
        bookingCall = Connector.getClient(Constants.BASE_URL).create(APIs.class).bookingDetails(requestBody);

        Connector connector = new Connector(this, BuildConfig.DEBUG);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(int statusCode, @Nullable String json, @NonNull String message) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    int response = jsonObject.getInt("response");
                    if (response == 1) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        model = BookingModel.toModel(data);
                        createBookingMarker();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    startCalling();
                }
            }

            @Override
            public void onFailure(boolean isNetworkIssue, @NonNull String errorMessage) {
                SessionApplication.toast(errorMessage);
            }
        });
        connector.Request("get_booking", bookingCall);
    }

    private void cancelGetBooking() {
        handler.removeCallbacks(runnable);
        if (bookingCall != null && !bookingCall.isCanceled()) {
            bookingCall.cancel();
        }
    }


    private void startCalling() {
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 1000 * 60);
    }

    private void endCalling() {
        cancelGetDriver();
        cancelGetBooking();
        handler.removeCallbacks(runnable);
    }

    Runnable runnable = () -> {
        getBooking();
        getDriver();
    };
}
