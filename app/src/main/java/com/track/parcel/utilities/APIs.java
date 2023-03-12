package com.track.parcel.utilities;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIs {

    @POST("user_login.php")
    Call<String> userLogin(@Body RequestBody requestBody);

    @POST("user_signup.php")
    Call<String> userRegistration(@Body RequestBody requestBody);

    @POST("user_set_profile.php")
    Call<String> userSetProfile(@Body RequestBody requestBody);

    @POST("user_get_profile.php")
    Call<String> userGetProfile(@Body RequestBody requestBody);

    @POST("user_forget_password.php")
    Call<String> userForgotPassword(@Body RequestBody requestBody);

    @POST("driver_login.php")
    Call<String> driverLogin(@Body RequestBody requestBody);

    @POST("driver_signup.php")
    Call<String> driverRegistration(@Body RequestBody requestBody);

    @POST("driver_set_profile.php")
    Call<String> driverSetProfile(@Body RequestBody requestBody);

    @POST("driver_get_profile.php")
    Call<String> driverGetProfile(@Body RequestBody requestBody);

    @POST("driver_forget_password.php")
    Call<String> driverForgotPassword(@Body RequestBody requestBody);

    @POST("driver_get_all.php")
    Call<String> driverGetAll();

    @POST("user_get_bookings.php")
    Call<String> userGetBookings(@Body RequestBody requestBody);

    @POST("driver_get_bookings.php")
    Call<String> driverGetBookings(@Body RequestBody requestBody);

    @POST("booking_create.php")
    Call<String> bookingCreate(@Body RequestBody requestBody);

    @POST("booking_update.php")
    Call<String> bookingUpdate(@Body RequestBody requestBody);

    @POST("booking_get_details.php")
    Call<String> bookingDetails(@Body RequestBody requestBody);

    @POST("feedback_create.php")
    Call<String> feedbackCreate(@Body RequestBody requestBody);

    @POST("feedback_get_details.php")
    Call<String> feedbackGetDetails(@Body RequestBody requestBody);
}