package com.example.travel_companion_app;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;

public class ProfileSetupActivity extends AppCompatActivity {

    private EditText etDob;
    private EditText etStartDate;
    private EditText etEndDate;
    private EditText etCitySearch;
    private ChipGroup chipGroupDestinations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        // Date picker fields
        etDob = findViewById(R.id.et_dob);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);

        etDob.setOnClickListener(v -> showDatePicker(etDob));
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        // City entry
        etCitySearch = findViewById(R.id.et_city_search);
        chipGroupDestinations = findViewById(R.id.chip_group_destinations);
        Button btnAddCity = findViewById(R.id.btn_add_city);

        btnAddCity.setOnClickListener(v -> addCityFromInput());

        etCitySearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addCityFromInput();
                return true;
            }
            return false;
        });
    }

    private void addCityFromInput() {
        String city = etCitySearch.getText().toString().trim();
        if (city.isEmpty()) return;
        addDestinationChip(city);
        etCitySearch.setText("");
    }

    private void addDestinationChip(String cityName) {
        Chip chip = new Chip(this);
        chip.setText(cityName);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#1E1E1E")));
        chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#00BFA5")));
        chip.setChipStrokeWidth(getResources().getDisplayMetrics().density);
        chip.setTextColor(Color.WHITE);
        chip.setCloseIconTint(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        chip.setOnCloseIconClickListener(v -> chipGroupDestinations.removeView(chip));
        chipGroupDestinations.addView(chip);
    }

    private void showDatePicker(EditText target) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String date = String.format("%02d/%02d/%04d", day, month + 1, year);
                    target.setText(date);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        if (target == etDob) {
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }
        dialog.show();
    }
}
