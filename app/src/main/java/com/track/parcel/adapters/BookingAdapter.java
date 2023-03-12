package com.track.parcel.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.track.parcel.BuildConfig;
import com.track.parcel.activities.BookingDetailsActivity;
import com.track.parcel.cells.BookingCell;
import com.track.parcel.models.BookingModel;
import com.track.parcel.models.CustomerModel;
import com.track.parcel.models.DriverModel;
import com.track.parcel.utilities.APIs;
import com.track.parcel.utilities.Constants;
import com.track.parcel.utilities.SessionApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import atirek.pothiwala.connection.Connector;
import atirek.pothiwala.utility.helper.Loader;
import okhttp3.RequestBody;
import retrofit2.Call;

public class BookingAdapter extends RecyclerView.Adapter<BookingCell> {

    private final Context context;
    private List<BookingModel> list = new ArrayList<>();
    private View viewEmpty;

    public BookingAdapter(Context context) {
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<BookingModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setViewEmpty(@Nullable View viewEmpty) {
        this.viewEmpty = viewEmpty;
    }

    @NonNull
    @Override
    public BookingCell onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return BookingCell.instance(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingCell cell, int position) {
        BookingModel model = list.get(position);
        cell.set(model);
        cell.onViewDetails(v -> {
            selectedBooking = model;
            getCustomer();
        });
    }

    @Override
    public int getItemCount() {
        int size = list.size();
        if (viewEmpty != null) {
            viewEmpty.setVisibility(size > 0 ? View.GONE : View.VISIBLE);
        }
        return size;
    }

    // --------------------- APIs -------------------------

    BookingModel selectedBooking;

    private void getCustomer() {
        JSONObject params = new JSONObject();
        try {
            params.put("id", selectedBooking.customerId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());
        Call<String> call = Connector.getClient(Constants.BASE_URL).create(APIs.class).userGetProfile(requestBody);

        Connector connector = new Connector(context, BuildConfig.DEBUG);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(int statusCode, @Nullable String json, @NonNull String message) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    int response = jsonObject.getInt("response");
                    if (response == 1) {
                        CustomerModel customer = CustomerModel.toModel(jsonObject.getJSONObject("data"));
                        getDriver(customer);
                    } else {
                        SessionApplication.toast(jsonObject.optString("message"));
                    }
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

        Loader loader = new Loader(context);
        loader.setCancelListener(dialog -> connector.cancelCall(call));

        connector.setLoaderDialog(loader.getDialog());
        connector.Request("get_user", call);
    }

    private void getDriver(CustomerModel customer) {
        JSONObject params = new JSONObject();
        try {
            params.put("id", selectedBooking.driverId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = Connector.createPartFromJsonObject(params.toString());
        Call<String> call = Connector.getClient(Constants.BASE_URL).create(APIs.class).driverGetProfile(requestBody);

        Connector connector = new Connector(context, BuildConfig.DEBUG);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(int statusCode, @Nullable String json, @NonNull String message) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    int response = jsonObject.getInt("response");
                    if (response == 1) {
                        DriverModel driver = DriverModel.toModel(jsonObject.getJSONObject("data"));

                        Intent intent = new Intent(context, BookingDetailsActivity.class);
                        intent.putExtra("driver", driver.toString());
                        intent.putExtra("customer", customer.toString());
                        intent.putExtra("booking", selectedBooking.toString());
                        context.startActivity(intent);
                    } else {
                        SessionApplication.toast(jsonObject.optString("message"));
                    }
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

        Loader loader = new Loader(context);
        loader.setCancelListener(dialog -> connector.cancelCall(call));

        connector.setLoaderDialog(loader.getDialog());
        connector.Request("get_driver", call);
    }
}
