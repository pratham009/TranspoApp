package com.track.parcel.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.track.parcel.BuildConfig;
import com.track.parcel.R;
import com.track.parcel.databinding.ActivityBookingDetailsBinding;
import com.track.parcel.models.BookingModel;
import com.track.parcel.models.CustomerModel;
import com.track.parcel.models.DriverModel;
import com.track.parcel.models.FeedbackModel;
import com.track.parcel.utilities.APIs;
import com.track.parcel.utilities.Constants;
import com.track.parcel.utilities.SessionApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import atirek.pothiwala.connection.Connector;
import atirek.pothiwala.utility.helper.DateHelper;
import atirek.pothiwala.utility.helper.IntentHelper;
import atirek.pothiwala.utility.helper.Loader;
import atirek.pothiwala.utility.helper.PermissionHelper;
import atirek.pothiwala.utility.helper.PhotoHelper;
import atirek.pothiwala.utility.helper.TextHelper;
import okhttp3.RequestBody;
import retrofit2.Call;


public class BookingDetailsActivity extends AppCompatActivity {

    private ActivityBookingDetailsBinding binding;
    private BookingModel booking;
    private CustomerModel customer;
    private DriverModel driver;
    private FeedbackModel feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        try {
            booking = BookingModel.toModel(new JSONObject(intent.getStringExtra("booking")));
            customer = CustomerModel.toModel(new JSONObject(intent.getStringExtra("customer")));
            driver = DriverModel.toModel(new JSONObject(intent.getStringExtra("driver")));
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        } finally {
            updateUI();

            binding.btnCustomerCall.setOnClickListener(v -> {
                phoneCall(customer.contact);
            });
            binding.btnDriverCall.setOnClickListener(v -> {
                phoneCall(driver.contact);
            });

            binding.ratingBar.setOnClickListener(null);
            binding.ratingBar.setOnRatingBarChangeListener(null);

            binding.btnAccept.setOnClickListener(v -> updateBooking("accepted"));
            binding.btnReject.setOnClickListener(v -> updateBooking("rejected"));
            binding.btnPickUp.setOnClickListener(v -> updateBooking("started"));
            binding.btnDeliver.setOnClickListener(v -> updateBooking("delivered"));
            binding.btnFeedback.setOnClickListener(v -> {
                Intent intentFeedback = new Intent(BookingDetailsActivity.this, FeedbackActivity.class);
                intentFeedback.putExtra("booking", booking.toString());
                startActivity(intentFeedback);
            });
            binding.btnTrack.setOnClickListener(v -> {
                Intent intent1 = new Intent(BookingDetailsActivity.this, TrackBookingActivity.class);
                intent1.putExtra("booking", booking.toString());
                startActivity(intent1);

            });

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFeedbacks();
    }

    private void updateUI() {
        binding.tvTitle.setText(String.format(Locale.getDefault(), getString(R.string.bookId), booking.id));

        String bookingDate = DateHelper.formatToString(booking.date, "yyyy-MM-dd", "dd MMM yyyy");
        binding.tvDate.setText(bookingDate);

        String status = TextHelper.capitalizeWord(booking.bookingStatus);
        binding.tvStatus.setText(status);
        checkStatus(status);

        new PhotoHelper("https://picsum.photos/200").placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile).load(binding.ivDriver);
        binding.tvDriver.setText(String.format(Locale.getDefault(), "%s (%s)", TextHelper.capitalizeWord(driver.name), driver.numberPlate.toUpperCase()));

        new PhotoHelper("https://picsum.photos/200").placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile).load(binding.ivCustomer);
        binding.tvCustomer.setText(TextHelper.capitalizeWord(customer.name));

        binding.etSource.setText(booking.source);
        binding.etDestination.setText(booking.destination);
        binding.etDescription.setText(booking.description);

        double[] summary = getSummary();
        double distance = summary[0];
        double vehicleCharges = summary[1];
        double serviceCharges = summary[2];
        double totalCharges = summary[3];

        binding.tvDistance.setText(String.format(Locale.getDefault(), "%.1f kms away", distance));
        binding.tvVehicleCharge.setText(String.format(Locale.getDefault(), "₹%.1f", vehicleCharges));
        binding.tvServiceCharges.setText(String.format(Locale.getDefault(), "₹%.1f", serviceCharges));
        binding.tvTotal.setText(String.format(Locale.getDefault(), "₹%.1f", totalCharges));

        binding.btnSource.setOnClickListener(v -> IntentHelper.googleMaps(this, String.format(Locale.getDefault(), "%s,%s", booking.sourceLatitude, booking.sourceLongitude)));
        binding.btnDestination.setOnClickListener(v -> IntentHelper.googleMaps(this, String.format(Locale.getDefault(), "%s,%s", booking.destinationLatitude, booking.destinationLongitude)));
    }

    private void checkStatus(String status) {
        boolean isCustomer = SessionApplication.isCustomer();
        boolean isFeedback = feedback != null;

        binding.btnDriverCall.setVisibility(isCustomer ? View.VISIBLE : View.GONE);
        binding.btnCustomerCall.setVisibility(!isCustomer ? View.VISIBLE : View.GONE);

        if (status.equalsIgnoreCase("pending")) {
            binding.tvStatus.setBackgroundResource(R.drawable.bg_orange);
        } else if (status.equalsIgnoreCase("accepted")) {
            binding.tvStatus.setBackgroundResource(R.drawable.bg_purple);
        } else if (status.equalsIgnoreCase("started")) {
            binding.tvStatus.setBackgroundResource(R.drawable.bg_blue);
        } else if (booking.bookingStatus.equalsIgnoreCase("rejected")) {
            binding.tvStatus.setBackgroundResource(R.drawable.bg_red);
        } else {
            binding.tvStatus.setBackgroundResource(R.drawable.bg_green);
        }

        if (isFeedback) {
            binding.etFeedback.setText(feedback.comments);
            binding.ratingBar.setRating(Float.parseFloat(feedback.rating));
            String feedbackDate = DateHelper.formatToString(feedback.date, "yyyy-MM-dd", "dd MMM yyyy");
            binding.tvFeedbackDate.setText(feedbackDate);
        }

        binding.layoutFeedback.setVisibility(isFeedback ? View.VISIBLE : View.GONE);
        if (isCustomer) {
            if (status.equalsIgnoreCase("accepted") || status.equalsIgnoreCase("started")) {
                binding.btnTrack.setVisibility(View.VISIBLE);
            } else {
                binding.btnTrack.setVisibility(View.GONE);
            }

            if (status.equalsIgnoreCase("delivered")) {
                binding.btnFeedback.setVisibility(isFeedback ? View.GONE : View.VISIBLE);
            } else {
                binding.btnFeedback.setVisibility(View.GONE);
            }

            binding.layoutButtons.setVisibility(View.GONE);
            binding.btnPickUp.setVisibility(View.GONE);
            binding.btnDeliver.setVisibility(View.GONE);
        } else {
            binding.btnTrack.setVisibility(View.GONE);
            binding.btnFeedback.setVisibility(View.GONE);

            if (status.equalsIgnoreCase("pending")) {
                binding.layoutButtons.setVisibility(View.VISIBLE);
            } else {
                binding.layoutButtons.setVisibility(View.GONE);
            }

            if (status.equalsIgnoreCase("accepted")) {
                binding.btnPickUp.setVisibility(View.VISIBLE);
            } else {
                binding.btnPickUp.setVisibility(View.GONE);
            }

            if (status.equalsIgnoreCase("started")) {
                binding.btnDeliver.setVisibility(View.VISIBLE);
            } else {
                binding.btnDeliver.setVisibility(View.GONE);
            }

        }
    }

    private double[] getSummary() {
        double distance = Double.parseDouble(booking.kms);
        double serviceCharges = Constants.SERVICE_CHARGES;
        double totalCharges = Double.parseDouble(booking.amount);
        double vehicleCharges = totalCharges - serviceCharges;

        return new double[]{distance, vehicleCharges, serviceCharges, totalCharges};
    }

    private void phoneCall(String phoneNumber) {
        String[] permissions = new String[]{Manifest.permission.CALL_PHONE};
        if (!PermissionHelper.checkPermissions(BookingDetailsActivity.this, permissions)) {
            PermissionHelper.requestPermissions(BookingDetailsActivity.this, permissions, 0);
            return;
        }
        IntentHelper.phoneCall(BookingDetailsActivity.this, phoneNumber);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateBooking(String status) {
        JSONObject params = new JSONObject();
        try {
            params.put("id", booking.id);
            params.put("status", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());
        Call<String> call = Connector.getClient(Constants.BASE_URL).create(APIs.class).bookingUpdate(requestBody);

        Connector connector = new Connector(this, BuildConfig.DEBUG);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(int statusCode, @Nullable String json, @NonNull String message) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    if (jsonObject.getInt("response") == 1) {
                        booking.bookingStatus = status;
                        updateUI();

                        if (status.equalsIgnoreCase("accepted")) {
                            updateDialog("Delivery Accepted");
                        } else if (status.equalsIgnoreCase("started")) {
                            updateDialog("Delivery Started");
                        } else if (status.equalsIgnoreCase("delivered")) {
                            updateDialog("Delivery Completed");
                        } else if (status.equalsIgnoreCase("rejected")) {
                            updateDialog("Delivery Rejected");
                        }
                    }
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
        connector.Request("update_booking", call);
    }

    private void updateDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(message);
        builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void getFeedbacks() {
        JSONObject params = new JSONObject();
        try {
            params.put("id", booking.id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());
        Call<String> call = Connector.getClient(Constants.BASE_URL).create(APIs.class).feedbackGetDetails(requestBody);

        Connector connector = new Connector(this, BuildConfig.DEBUG);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(int statusCode, @Nullable String json, @NonNull String message) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    int response = jsonObject.getInt("response");
                    if (response == 1) {
                        feedback = FeedbackModel.toModel(jsonObject.getJSONObject("data"));
                        updateUI();
                    }
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
        connector.Request("get_feedback", call);
    }

}