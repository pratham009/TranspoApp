package com.track.parcel.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.track.parcel.R;
import com.track.parcel.databinding.ActivitySplashBinding;
import com.track.parcel.utilities.SessionApplication;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        handler.postDelayed(runnable, 2000);
    }

    boolean isCustomer() {
        return SessionApplication.storage().get("type").equalsIgnoreCase(getString(R.string.customer));
    }

    Runnable runnable = () -> {
        if (SessionApplication.storage().exists()) {
            if (isCustomer()) {
                startActivity(new Intent(SplashActivity.this, CustomerHomeActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, DriverHomeActivity.class));
            }
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
    };
}