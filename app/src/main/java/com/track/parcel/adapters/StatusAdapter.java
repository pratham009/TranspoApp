package com.track.parcel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.track.parcel.R;
import com.track.parcel.databinding.CellVehicleBinding;

import java.util.Arrays;
import java.util.List;

import atirek.pothiwala.utility.helper.TextHelper;
import atirek.pothiwala.utility.helper.Tools;

public class StatusAdapter extends ArrayAdapter<String> {

    private final LayoutInflater inflater;
    private final List<String> list = Arrays.asList("Pending", "Accepted", "Started", "Delivered");

    public StatusAdapter(@NonNull Context context) {
        super(context, R.layout.cell_vehicle);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        convertView = getView(position, convertView, parent);
        convertView.setBackgroundColor(Tools.getColor(parent.getContext(), R.color.white));
        return convertView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public String getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String status = list.get(position);

        CellVehicleBinding binding;
        if (convertView == null) {
            binding = CellVehicleBinding.inflate(inflater, parent, false);
        } else {
            binding = CellVehicleBinding.bind(convertView);
        }
        binding.tvTitle.setText(TextHelper.capitalizeWord(status));

        convertView = binding.getRoot();
        return convertView;
    }
}
