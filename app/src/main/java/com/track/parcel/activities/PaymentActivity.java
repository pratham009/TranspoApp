package com.track.parcel.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.track.parcel.R;
import com.track.parcel.databinding.ActivityPaymentBinding;
import com.track.parcel.utilities.SessionApplication;


public class PaymentActivity extends AppCompatActivity {

    ActivityPaymentBinding binding;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.ivProgress.setVisibility(View.GONE);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        handler.postDelayed(processingRunnable, 5000);
    }

    Runnable processingRunnable = new Runnable() {
        @Override
        public void run() {
            binding.barProgress.setVisibility(View.GONE);
            binding.ivProgress.setVisibility(View.VISIBLE);

            binding.tvProgress.setText(getString(R.string.paymentCompleted));
            handler.postDelayed(waitRunnable, 2500);
        }
    };

    Runnable waitRunnable = () -> {
        SessionApplication.toast("Booking Successful");
        finish();
        startActivity(new Intent(this, BookingsActivity.class));
    };
}