package com.track.parcel.utilities;

import android.animation.ValueAnimator;
import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Constants {
   public static final String SESSION_NAME = "parcel-app";
   public static final String BASE_URL ="http://192.168.213.84/TMP/API/";
   public static final double SERVICE_CHARGES = 12.0;

   public static void animateMarker(@NonNull Location location, @NonNull Marker marker) {
      final LatLng startPosition = marker.getPosition();
      final LatLng endPosition = new LatLng(location.getLatitude(), location.getLongitude());
      final float startRotation = marker.getRotation();
      final float endRotation = location.getBearing();

      final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
      ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
      valueAnimator.setDuration(2000); // duration 1 second
      valueAnimator.addUpdateListener(animation -> {
         try {
            float v = animation.getAnimatedFraction();
            LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
            marker.setPosition(newPosition);
            marker.setRotation(computeRotation(v, startRotation, endRotation));

         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      valueAnimator.start();
   }

   private static float computeRotation(float fraction, float start, float end) {
      float normalizeEnd = end - start; // rotate start to 0
      float normalizedEndAbs = (normalizeEnd + 360) % 360;

      float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
      float rotation;
      if (direction > 0) {
         rotation = normalizedEndAbs;
      } else {
         rotation = normalizedEndAbs - 360;
      }

      float result = fraction * rotation + start;
      return (result + 360) % 360;
   }

   private interface LatLngInterpolator {
      LatLng interpolate(float fraction, LatLng a, LatLng b);

      class LinearFixed implements LatLngInterpolator {
         @Override
         public LatLng interpolate(float fraction, LatLng a, LatLng b) {
            double lat = (b.latitude - a.latitude) * fraction + a.latitude;
            double lngDelta = b.longitude - a.longitude;
            // Take the shortest path across the 180th meridian.
            if (Math.abs(lngDelta) > 180) {
               lngDelta -= Math.signum(lngDelta) * 360;
            }
            double lng = lngDelta * fraction + a.longitude;
            return new LatLng(lat, lng);
         }
      }
   }
}
