package com.example.travel_companion_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etDob;
    private RadioGroup rgGender;
    private EditText etCountry;
    private ChipGroup chipGroupDestinations;
    private EditText etStartDate;
    private EditText etEndDate;
    private ChipGroup chipGroupHobbies;
    private EditText etContact;
    private EditText etCitySearch;
    private MaterialButton btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        etName = findViewById(R.id.et_name);
        etDob = findViewById(R.id.et_dob);
        rgGender = findViewById(R.id.rg_gender);
        etCountry = findViewById(R.id.et_country);
        chipGroupDestinations = findViewById(R.id.chip_group_destinations);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);
        chipGroupHobbies = findViewById(R.id.chip_group_hobbies);
        etContact = findViewById(R.id.et_contact);
        btnSaveProfile = findViewById(R.id.btn_save_profile);

        // Date picker fields
        etDob.setOnClickListener(v -> showDatePicker(etDob));
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        // City entry
        etCitySearch = findViewById(R.id.et_city_search);
        Button btnAddCity = findViewById(R.id.btn_add_city);

        btnAddCity.setOnClickListener(v -> addCityFromInput());

        etCitySearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addCityFromInput();
                return true;
            }
            return false;
        });

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        List<String> destinations = getChipTexts(chipGroupDestinations);
        if (destinations.isEmpty()) {
            Toast.makeText(this, "Please add at least one destination", Toast.LENGTH_SHORT).show();
            return;
        }

        String dob = etDob.getText().toString().trim();
        String country = etCountry.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        List<String> hobbies = getSelectedHobbyChips();

        String gender = "";
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            RadioButton selectedButton = findViewById(selectedGenderId);
            gender = selectedButton.getText().toString();
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        Map<String, Object> profile = new HashMap<>();
        profile.put("name", name);
        profile.put("dob", dob);
        profile.put("gender", gender);
        profile.put("country", country);
        profile.put("destinations", destinations);
        profile.put("startDate", startDate);
        profile.put("endDate", endDate);
        profile.put("hobbies", hobbies);
        profile.put("contact", contact);

        userRef.setValue(profile)
                .addOnSuccessListener(unused -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("Save Profile");
                });
    }

    private List<String> getChipTexts(ChipGroup chipGroup) {
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            texts.add(chip.getText().toString());
        }
        return texts;
    }

    private List<String> getSelectedHobbyChips() {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < chipGroupHobbies.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupHobbies.getChildAt(i);
            if (chip.isChecked()) {
                selected.add(chip.getText().toString());
            }
        }
        return selected;
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
