package com.track.parcel.cells;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.track.parcel.R;
import com.track.parcel.databinding.CellBookingBinding;
import com.track.parcel.models.BookingModel;

import java.util.Locale;

import atirek.pothiwala.utility.helper.DateHelper;
import atirek.pothiwala.utility.helper.TextHelper;


public class BookingCell extends RecyclerView.ViewHolder {

    private final CellBookingBinding binding;

    public static BookingCell instance(ViewGroup group) {
        View view = LayoutInflater.from(group.getContext()).inflate(R.layout.cell_booking, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new BookingCell(view);
    }

    public BookingCell(@NonNull View itemView) {
        super(itemView);
        binding = CellBookingBinding.bind(itemView);
    }

    public void set(BookingModel model) {
        binding.tvBookingId.setText(String.format(Locale.getDefault(), "#BOOK_%s", model.id));
        binding.tvCharges.setText(String.format(Locale.getDefault(), "%s Rs", model.amount));

        binding.tvSource.setText(TextHelper.capitalizeSentence(model.source));
        binding.tvDestination.setText(TextHelper.capitalizeSentence(model.destination));

        String bookingDate = DateHelper.formatToString(model.date, "yyyy-MM-dd", "dd MMM yyyy");
        String bookingStatus = TextHelper.capitalizeWord(model.bookingStatus);

        binding.tvDate.setText(bookingDate);
        binding.tvDistance.setText(String.format(Locale.getDefault(), "%s kms away - %s", model.kms, bookingStatus));

        binding.btnViewDetails.setVisibility(View.VISIBLE);
    }

    public void onViewDetails(View.OnClickListener listener) {
        binding.btnViewDetails.setOnClickListener(listener);
    }

}
