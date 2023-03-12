package com.track.parcel.utilities;

import android.widget.Toast;

import com.track.parcel.R;

import atirek.pothiwala.utility.components.MyApplication;
import atirek.pothiwala.utility.helper.Session;

public class SessionApplication extends MyApplication {

    private static Session session;

    public static Session storage() {
        if (session == null) {
            session = new Session(shared().getApplicationContext(), Constants.SESSION_NAME);
        }
        return session;
    }

    public static void toast(String message) {
        if(!message.trim().isEmpty()){
            Toast.makeText(shared().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isCustomer() {
        return storage().get("type").equalsIgnoreCase(shared().getApplicationContext().getString(R.string.customer));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        session = new Session(this, "transpo");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        session = null;
    }


}
